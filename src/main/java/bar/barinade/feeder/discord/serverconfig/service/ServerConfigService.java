package bar.barinade.feeder.discord.serverconfig.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bar.barinade.feeder.discord.serverconfig.data.ServerConfiguration;
import bar.barinade.feeder.discord.serverconfig.repo.ServerConfigurationRepo;
@Service
public class ServerConfigService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ServerConfigService.class);

	@Autowired
	private ServerConfigurationRepo configRepo;
	
	@Transactional
	public ServerConfiguration getConfig(Long guildId) {
		ServerConfiguration config = configRepo.findById(guildId).orElse(null);
		if (config == null) {
			config = new ServerConfiguration();
			config.setId(guildId);
			config = configRepo.saveAndFlush(config);
		}
		return config;
	}
	
	@Transactional
	public void setOutputChannel(Long guildId, Long channelId) {
		ServerConfiguration config = getConfig(guildId);
		config.setChannelId(channelId);
		configRepo.saveAndFlush(config);
		m_logger.info("Guild {} set output channel to {}", guildId, channelId);
	}
	
	@Transactional
	public Long getOutputChannel(Long guildId) {
		return getConfig(guildId).getChannelId();
	}
	
	@Transactional
	public void setHumbleBundlePartnerCode(Long guildId, String code) {
		ServerConfiguration config = getConfig(guildId);
		config.setHumbleBundlePartnerCode(code);
		configRepo.saveAndFlush(config);
		m_logger.info("Guild {} set humble bundle partner code to {}", guildId, code);
	}
	
	@Transactional
	public String getHumbleBundlePartnerCode(Long guildId) {
		String code = getConfig(guildId).getHumbleBundlePartnerCode();
		if (code == null) {
			code = "";
		}
		return code;
	}
	
	@Transactional
	public void setPostHumbleBundleLinks(Long guildId, boolean b) {
		ServerConfiguration config = getConfig(guildId);
		config.setSendHumbleBundle(b);
		configRepo.saveAndFlush(config);
		m_logger.info("Guild {} set humble bundle posting to {}", guildId, b);
	}
	
	@Transactional
	public Boolean getPostHumbleBundleLinks(Long guildId) {
		return getConfig(guildId).getSendHumbleBundle();
	}
	
}
