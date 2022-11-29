package bar.barinade.feeder.discord.serverconfig.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bar.barinade.feeder.discord.serverconfig.data.Subreddit;
import bar.barinade.feeder.discord.serverconfig.data.pk.SubredditId;

@Repository
public interface SubredditRepo extends JpaRepository<Subreddit, SubredditId> {
	
	List<Subreddit> findByIdGuildId(Long id);
	Long deleteByIdGuildId(Long id);
	List<Subreddit> findByIdName(String name);
	Long deleteByIdGuildIdAndIdName(Long id, String name);

}
