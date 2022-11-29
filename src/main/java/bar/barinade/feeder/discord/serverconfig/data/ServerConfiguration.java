package bar.barinade.feeder.discord.serverconfig.data;


import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "server_configs")
public class ServerConfiguration {
	
	@Id
	@Column(name = "guild_id")
	private Long id;
	
	@Column(name = "channel_id", nullable = true)
	private Long channelId;
	
	@OneToMany(mappedBy = "id.guild")
	private Set<Subreddit> subreddits;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Set<Subreddit> getSubreddits() {
		return subreddits;
	}

	public void setSubreddits(Set<Subreddit> subreddits) {
		this.subreddits = subreddits;
	}

}
