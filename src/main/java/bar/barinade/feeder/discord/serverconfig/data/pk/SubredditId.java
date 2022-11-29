package bar.barinade.feeder.discord.serverconfig.data.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import bar.barinade.feeder.discord.serverconfig.data.ServerConfiguration;

@Embeddable
public class SubredditId implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "guild_id", nullable = false)
	private ServerConfiguration guild;

	public SubredditId() {}

	public SubredditId(String name, ServerConfiguration guild) {
		this.name = name;
		this.guild = guild;
	}

	public ServerConfiguration getGuild() {
		return guild;
	}

	public void setGuild(ServerConfiguration guild) {
		this.guild = guild;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(guild, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubredditId other = (SubredditId) obj;
		return Objects.equals(guild, other.guild) && Objects.equals(name, other.name);
	}
	
}
