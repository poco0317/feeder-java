package bar.barinade.feeder.discord.serverconfig.data;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import bar.barinade.feeder.discord.serverconfig.data.pk.SubredditId;

@Entity
@Table(name = "subreddits")
public class Subreddit {

	@EmbeddedId
	private SubredditId id;
	
	@Column(name = "upvote_threshold", nullable = false)
	private Integer upvoteThreshold;

	public SubredditId getId() {
		return id;
	}

	public void setId(SubredditId id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, upvoteThreshold);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subreddit other = (Subreddit) obj;
		return Objects.equals(id, other.id) && Objects.equals(upvoteThreshold, other.upvoteThreshold);
	}

	public Integer getUpvoteThreshold() {
		return upvoteThreshold;
	}

	public void setUpvoteThreshold(Integer upvoteThreshold) {
		this.upvoteThreshold = upvoteThreshold;
	}
	
}
