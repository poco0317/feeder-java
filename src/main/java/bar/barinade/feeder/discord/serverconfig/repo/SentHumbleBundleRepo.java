package bar.barinade.feeder.discord.serverconfig.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bar.barinade.feeder.discord.serverconfig.data.SentHumbleBundle;

@Repository
public interface SentHumbleBundleRepo extends JpaRepository<SentHumbleBundle, Long> {
	
	List<SentHumbleBundle> findByGuildId(Long guildId);
	
	Long deleteBySnowflake(Long snowflake);
	
}
