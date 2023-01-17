package bar.barinade.feeder.discord.serverconfig.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sent_humblebundles")
public class SentHumbleBundle {
	
	@Id
	@Column(name = "snowflake", nullable = false)
	private Long snowflake;
	
	@Column(name = "post_time", nullable = false)
	private Long time;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "guild_id", nullable = false)
	private Long guildId;

	public Long getSnowflake() {
		return snowflake;
	}

	public void setSnowflake(Long snowflake) {
		this.snowflake = snowflake;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

}
