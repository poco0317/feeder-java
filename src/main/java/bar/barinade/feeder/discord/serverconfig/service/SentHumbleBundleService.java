package bar.barinade.feeder.discord.serverconfig.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bar.barinade.feeder.discord.serverconfig.data.SentHumbleBundle;
import bar.barinade.feeder.discord.serverconfig.repo.SentHumbleBundleRepo;

@Service
public class SentHumbleBundleService {
		
	@Autowired
	private SentHumbleBundleRepo repo;
	
	@Transactional
	public void deleteBySnowflake(Long id) {
		repo.deleteBySnowflake(id);
	}
	
	@Transactional
	public SentHumbleBundle getBySnowflake(Long id) {
		return repo.findById(id).orElse(null);
	}
	
	@Transactional
	public List<SentHumbleBundle> getByGuild(Long guildId) {
		return repo.findByGuildId(guildId);
	}
	
	@Transactional
	public void emplace(Long snowflake, Long time, String name, Long guildId) {
		SentHumbleBundle m = new SentHumbleBundle();
		m.setSnowflake(snowflake);
		m.setTime(time);
		m.setGuildId(guildId);
		m.setName(name);
		repo.save(m);
	}
}
