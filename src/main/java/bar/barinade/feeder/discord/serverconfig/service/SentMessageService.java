package bar.barinade.feeder.discord.serverconfig.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bar.barinade.feeder.discord.serverconfig.data.SentMessage;
import bar.barinade.feeder.discord.serverconfig.repo.SentMessageRepo;

@Service
public class SentMessageService {
		
	@Autowired
	private SentMessageRepo repo;

	@Transactional
	public void deleteByGuid(String guid) {
		repo.deleteByGuid(guid);
	}
	
	@Transactional
	public void deleteBySnowflake(Long id) {
		repo.deleteBySnowflake(id);
	}
	
	@Transactional
	public SentMessage getBySnowflake(Long id) {
		return repo.findById(id).orElse(null);
	}
	
	@Transactional
	public SentMessage getByGuid(String guid) {
		List<SentMessage> msgs = repo.findByGuid(guid);
		if (msgs == null || msgs.size() == 0) {
			return null;
		}
		return msgs.get(0);
	}
	
	@Transactional
	public List<SentMessage> getByGuild(Long guildId) {
		return repo.findByGuildId(guildId);
	}
	
	@Transactional
	public SentMessage getByGuidAndGuild(Long guildId, String guid) {
		List<SentMessage> msgs = repo.findByGuidAndGuildId(guid, guildId);
		if (msgs == null || msgs.size() == 0) {
			return null;
		}
		return msgs.get(0);
	}
	
	@Transactional
	public void emplace(Long snowflake, Long time, String guid, Long guildId) {
		SentMessage m = new SentMessage();
		m.setGuid(guid);
		m.setSnowflake(snowflake);
		m.setTime(time);
		m.setGuildId(guildId);
		repo.save(m);
	}
}
