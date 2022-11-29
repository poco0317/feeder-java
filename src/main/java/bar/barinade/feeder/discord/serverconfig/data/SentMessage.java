package bar.barinade.feeder.discord.serverconfig.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sent_messages")
public class SentMessage {
	
	@Id
	@Column(name = "snowflake", nullable = false)
	private Long snowflake;
	
	@Column(name = "time", nullable = false)
	private Long time;
	
	// like a reddit id
	@Column(name = "guid", nullable = false)
	private String guid;
	
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

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

}
