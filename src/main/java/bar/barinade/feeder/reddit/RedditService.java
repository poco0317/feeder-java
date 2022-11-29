package bar.barinade.feeder.reddit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;

@Service
public class RedditService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(RedditService.class);
	
	private static final int POSTS_PER_REQ = 100;
	private static final int PAGES_TO_FETCH = 5;
	
	@Value("${reddit.clientid}")
	private String clientId;
	
	@Value("${reddit.clientsecret}")
	private String clientSecret;
	
	// reddit cache
	private Cache cache = new Cache();
	
	private Reddit4J client = null;

	@PostConstruct
	private void init() {
		m_logger.info("Starting RedditService");
		
		client = Reddit4J
				.rateLimited()
				.setClientId(clientId)
				.setClientSecret(clientSecret)
				.setUserAgent(
						new UserAgentBuilder()
						.appname("barinade-feeder")
						.author("legitimatecookies")
						.version("0.1")
				);
		try {
			client.userlessConnect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		m_logger.info("Started RedditService");
	}
	
	private List<RedditPost> retriedGetSubredditPosts(String subreddit, int count) {
		try {
			return client.getSubredditPosts(subreddit, Sorting.NEW).count(count).limit(POSTS_PER_REQ).submit();
		} catch (Exception e) {
			try {
				m_logger.info("Failed to get subreddit posts for {} ... reseting client connection", subreddit);
				client.userlessConnect();
				return client.getSubredditPosts(subreddit, Sorting.NEW).count(count).limit(POSTS_PER_REQ).submit(); 
			} catch (Exception ee) {
				m_logger.error(e.getMessage(), e);
				return new ArrayList<RedditPost>();
			}
		}
	}
	
	public void cacheSub(String subreddit) throws IOException, InterruptedException, AuthenticationException {
		List<RedditPost> posts = retriedGetSubredditPosts(subreddit, 0);
		Set<String> postIds = posts.stream().map(p -> p.getId()).collect(Collectors.toSet());
		if (posts != null && posts.size() == POSTS_PER_REQ) {
			m_logger.trace("Subreddit cache - SUB {} - running loop ...", subreddit);
			// if we found as many posts as allowed, just go ahead and fetch X pages worth
			int cnt = (POSTS_PER_REQ * PAGES_TO_FETCH) - posts.size();
			while (cnt > 0) {
				List<RedditPost> page = retriedGetSubredditPosts(subreddit, posts.size());
				if (page.size() > 0) {
					m_logger.trace(" count {} and pulled {}", cnt, page.size());
					cnt -= page.size();
					page.forEach(post -> {
						if (!postIds.contains(post.getId())) {
							postIds.add(post.getId());
							posts.add(post);
						}
					});
				} else {
					m_logger.trace(" count {}", cnt);
					cnt = 0;
				}
			}
		}
		cache.intake(subreddit, posts);
	}
	
	public List<RedditPost> get(String sub) {
		return cache.get(sub);
	}
	
	private class Cache {
		// subreddits to lists of entries
		private ConcurrentHashMap<String, Set<RedditPost>> entries = new ConcurrentHashMap<>();
		private static final long MAX_POST_AGE_SEC = 60L * 60L * 4L; // 4 hours
		
		private void clean(String sub) {
			if (!entries.containsKey(sub)) {
				entries.put(sub, ConcurrentHashMap.newKeySet());
			}
			long now = System.currentTimeMillis() / 1000;
			entries.get(sub).removeIf(p -> now - p.getCreated() > MAX_POST_AGE_SEC);
		}
		
		public void intake(String sub, List<RedditPost> posts) {
			if (sub == null || posts == null) {
				return;
			}
			clean(sub);
			Set<String> things = entries.get(sub).stream().map(p -> p.getId()).collect(Collectors.toSet());
			HashMap<String, RedditPost> idToPost = new HashMap<>();
			entries.get(sub).forEach(p -> {
				idToPost.put(p.getId(), p);
			});
			posts.forEach(p -> {
				if (!things.contains(p.getId())) {
					entries.get(sub).add(p);
				} else {
					// update stale posts
					idToPost.get(p.getId()).setUps(p.getUps());
				}
			});
			
			clean(sub);
		}
		
		public List<RedditPost> get(String sub) {
			clean(sub);
			return new ArrayList<>(entries.get(sub));
		}
	}
	
}
