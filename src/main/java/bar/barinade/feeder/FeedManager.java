package bar.barinade.feeder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import bar.barinade.feeder.discord.BotManager;
import bar.barinade.feeder.discord.serverconfig.data.SentHumbleBundle;
import bar.barinade.feeder.discord.serverconfig.data.SentMessage;
import bar.barinade.feeder.discord.serverconfig.data.ServerConfiguration;
import bar.barinade.feeder.discord.serverconfig.data.Subreddit;
import bar.barinade.feeder.discord.serverconfig.service.SentHumbleBundleService;
import bar.barinade.feeder.discord.serverconfig.service.SentMessageService;
import bar.barinade.feeder.discord.serverconfig.service.ServerConfigService;
import bar.barinade.feeder.discord.serverconfig.service.SubredditService;
import bar.barinade.feeder.humblebundle.Bundle;
import bar.barinade.feeder.humblebundle.HumbleBundleScrapeService;
import bar.barinade.feeder.reddit.RedditService;
import masecla.reddit4j.objects.RedditPost;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@Service
public class FeedManager {
	
	private static final Logger m_logger = LoggerFactory.getLogger(FeedManager.class);
	
	@Autowired
	private RedditService reddit;
	
	@Autowired
	private HumbleBundleScrapeService humbleBundleScraper;
	
	@Autowired
	private SubredditService subService;
	
	@Autowired
	private ServerConfigService config;
	
	@Autowired
	private SentMessageService posted;
	
	@Autowired
	private SentHumbleBundleService postedHumble;
	
	@Scheduled(fixedDelay = 1000L * 60L * 5L)
	private void refresh() {
		m_logger.info("Feed refresh started");
		
		List<Subreddit> subs = new ArrayList<>();
		HashMap<Long, List<Subreddit>> guildsubs = new HashMap<>();
		for (Guild g : BotManager.getJDA().getGuilds()) {
			final Long id = g.getIdLong();
			final List<Subreddit> l = subService.getByGuild(id);
			subs.addAll(l);
			guildsubs.put(id, l);
		}
		
		m_logger.info("Feed refresh beginning Reddit Phase");
		HashMap<String, List<RedditPost>> posts = new HashMap<>();
		try {
			for (Subreddit s : subs) {
				final String name = s.getId().getName();
				reddit.cacheSub(name);
				posts.put(name, reddit.get(name));
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		
		m_logger.debug("Collected {} posts in {} subs", posts.values().stream().map(l -> l.size()).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum(), subs.size());
		
		// operate for each guild, each sub which they subscribe to
		for (Entry<Long, List<Subreddit>> entry : guildsubs.entrySet()) {
			final Long guildId = entry.getKey();
			// track ids of all posts scanned
			HashSet<String> foundIds = new HashSet<>();
			long cnt = 0;
			
			for (Subreddit sub : entry.getValue()) {
				final String subname = sub.getId().getName();
				final Integer threshold = sub.getUpvoteThreshold();
				if (!posts.containsKey(subname)) continue;
				for (RedditPost post : posts.get(subname)) {
					// skip text posts
					if (post.is_self()) {
						m_logger.debug("Post {} is self post, skipped", post.getId());
						continue;
					}
					
					final String postId = post.getId();
					foundIds.add(postId);
					
					// if the item was not in the list and qualifies, put it in and make a new post
					if (post.getUps() >= threshold && posted.getByGuidAndGuild(guildId, postId) == null) {
						postContent(guildId, post);
						cnt++;
					}
				}
			}
			
			if (cnt > 0L) {
				m_logger.info("Made {} posts in guild {}", cnt, guildId);
			}
			
			m_logger.debug("foundIds size {}", foundIds.size());
			// clean up the cached messages
			List<SentMessage> msgs = posted.getByGuild(guildId);
			for (SentMessage msg : msgs) {
				if (!foundIds.contains(msg.getGuid())) {
					posted.deleteBySnowflake(msg.getSnowflake());
					m_logger.debug("Deleted cached posted message {}", msg.getGuid());
				}
			}
		}
		m_logger.info("Feed refresh finished Reddit Phase");
		
		m_logger.info("Feed refresh beginning HumbleBundle Phase");
		List<Bundle> currentBundles = humbleBundleScraper.scrape();
		for (Guild g : BotManager.getJDA().getGuilds()) {
			final Long id = g.getIdLong();
			
			if (!config.getConfig(id).getSendHumbleBundle()) {
				continue;
			}
			
			List<SentHumbleBundle> sentBundles = postedHumble.getByGuild(id);
			
			Function<Bundle, Boolean> isBundleAlreadySent = bundle -> {
				for (SentHumbleBundle b : sentBundles) {
					if (b.getName().equalsIgnoreCase(bundle.name())) {
						return true;
					}
				}
				return false;
			};
			
			for (Bundle bundle : currentBundles) {
				
				if (isBundleAlreadySent.apply(bundle)) {
					continue;
				}
				
				postBundle(id, bundle);
			}
		}
		m_logger.info("Feed refresh finished HumbleBundle Phase");
		
		
		m_logger.info("Feed refresh finished");
	}
	
	private void postBundle(Long guildId, Bundle bundle) {
		m_logger.trace("Posting bundle in guild {}", guildId);
		JDA jda = BotManager.getJDA();
		
		
		ServerConfiguration cfg = config.getConfig(guildId);
		Long channelId = cfg.getChannelId();
		if (channelId != null) {
			TextChannel chan = jda.getTextChannelById(channelId);
			if (chan != null) {
				String partnerCode = cfg.getHumbleBundlePartnerCode();
				if (partnerCode == null) {
					partnerCode = "";
				} else if (!partnerCode.isEmpty()) {
					partnerCode = "?partner=" + partnerCode;
				}
				String url = bundle.url();
				if (url == null) {
					url = "";
				}
				String fmtstr = String.format("New Humble Bundle!\n%s\n%s", bundle.name(), url + partnerCode);
				chan.sendMessage(fmtstr).queue(msg -> {
					postedHumble.emplace(msg.getIdLong(), System.currentTimeMillis(), bundle.name(), guildId);
				});
			}
		}
	}
	
	private void postContent(Long guildId, RedditPost post) {
		m_logger.trace("Posting content in guild {}", guildId);
		JDA jda = BotManager.getJDA();
		
		final boolean selfpost = post.is_self();
		if (!selfpost) {
			final String author = "Post by "+nullcheck(post.getAuthor());
			final String titleTxt = "New link post";
			final String desc = MarkdownUtil.bold(nullcheck(post.getTitle())) + "\n" + post.getUrl();
			final String link = post.getPermalink().startsWith("http") ? post.getPermalink() : "https://reddit.com" + post.getPermalink();
			m_logger.info("{}", post.getUrl());
			MessageEmbed emb = new EmbedBuilder()
					.setTitle(titleTxt, link)
					.setDescription(desc)
					.setColor(0xbd0000)
					.setTimestamp(Instant.ofEpochSecond(post.getCreated()))
					.setAuthor(author)
					.setFooter("Reddit - /r/"+nullcheck(post.getSubreddit()), jda.getSelfUser().getEffectiveAvatarUrl())
					.setImage(post.getUrl())
					.build();
			Long channelId = config.getConfig(guildId).getChannelId();
			if (channelId != null) {
				TextChannel chan = jda.getTextChannelById(channelId);
				if (chan != null) {
					chan.sendMessageEmbeds(emb).queue(msg -> {
						posted.emplace(msg.getIdLong(), System.currentTimeMillis(), post.getId(), guildId);
					});
				}
			}
		}
	}
	
	private String nullcheck(String s) {
		return s == null ? "" : s;
	}

}
