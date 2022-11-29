package bar.barinade.feeder.discord.serverconfig.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bar.barinade.feeder.discord.serverconfig.data.SentMessage;

@Repository
public interface SentMessageRepo extends JpaRepository<SentMessage, Long> {
	
	List<SentMessage> findByGuid(String guid);
	List<SentMessage> findByGuildId(Long guildId);
	List<SentMessage> findByGuidAndGuildId(String guid, Long guildId);
	
	Long deleteBySnowflake(Long snowflake);
	Long deleteByGuid(String guid);
	
}
