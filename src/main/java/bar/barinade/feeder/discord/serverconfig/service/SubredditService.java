package bar.barinade.feeder.discord.serverconfig.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bar.barinade.feeder.discord.serverconfig.data.Subreddit;
import bar.barinade.feeder.discord.serverconfig.data.pk.SubredditId;
import bar.barinade.feeder.discord.serverconfig.repo.SubredditRepo;

@Service
public class SubredditService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(SubredditService.class);
		
	@Autowired
	private ServerConfigService configService;
	
	@Autowired
	private SubredditRepo subs;
	
	/**
	 * Returns false if the subreddit already exists, so the upvote count was changed
	 */
	@Transactional
	public boolean set(Long guildId, String name, Integer upvoteThreshold) {
		m_logger.info("Setting new subreddit | GUILD {} | NAME {} | UPVOTES {}", guildId, name, upvoteThreshold);
		SubredditId id = new SubredditId(name, configService.getConfig(guildId));
		Subreddit sub = subs.findById(id).orElse(null); 
		if (sub == null) {
			sub = new Subreddit();
			sub.setId(id);
			sub.setUpvoteThreshold(upvoteThreshold);
			subs.save(sub);
			
			m_logger.info("Made new subreddit {} for guild {}", name, guildId);
			
			return true;
		} else {
			sub.setUpvoteThreshold(upvoteThreshold);
			
			subs.save(sub);
			m_logger.info("Subreddit {} guild {} already existed, updated upvote count", name, guildId);
			return false;
		}
	}
	
	@Transactional
	public boolean deleteSub(Long guildId, String name) {
		m_logger.info("Deleting subreddit | GUILD {} | NAME {}", guildId, name);
		return subs.deleteByIdGuildIdAndIdName(guildId, name) > 0L;
	}
	
	@Transactional
	public boolean deleteGuild(Long guildId) {
		m_logger.info("Deleting guild | GUILD {}", guildId);
		return subs.deleteByIdGuildId(guildId) > 0L;
	}
	
	@Transactional
	public Subreddit get(Long guildId, String name) {
		return subs.findById(new SubredditId(name, configService.getConfig(guildId))).orElse(null);
	}
	
	@Transactional
	public List<Subreddit> getByGuild(Long guildId) {
		return subs.findByIdGuildId(guildId);
	}
	
	@Transactional
	public List<Subreddit> getByName(String name) {
		return subs.findByIdName(name);
	}

}
