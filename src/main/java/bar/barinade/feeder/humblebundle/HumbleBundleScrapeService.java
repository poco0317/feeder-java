package bar.barinade.feeder.humblebundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@Service
public class HumbleBundleScrapeService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(HumbleBundleScrapeService.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Value("${humblebundle.url}")
	private String bundleUrl;
	
	@PostConstruct
	private void init() {
		m_logger.info("Starting HumbleBundleScrapeService");
		
		
		
		m_logger.info("Started HumbleBundleScrapeService");
	}
	
	/**
	 * Scrape the humblebundle site and see what bundles appear
	 */
	public List<Bundle> scrape() {
		
		try {
			Document doc = Jsoup.connect(bundleUrl).get();
			
			Element jsonScriptElement = doc.getElementById("landingPage-json-data");
			String json = jsonScriptElement.text();
			m_logger.info(doc.toString());
			m_logger.info(json);
			
			ObjectReader reader = mapper.readerFor(Map.class);
			Map<?,?> map = reader.readValue(json);
			
			Map<?,?> data = (Map<?, ?>)map.get("data");
			Map<?,?> bookObj = (Map<?, ?>) data.get("books");
			Map<?,?> gamesObj = (Map<?, ?>) data.get("games");
			Map<?,?> softwareObj = (Map<?, ?>) data.get("software");
			
			List<Bundle> allBundles = new ArrayList<>();
			allBundles.addAll(extractInfo(bookObj));
			allBundles.addAll(extractInfo(gamesObj));
			allBundles.addAll(extractInfo(softwareObj));
			
			return allBundles;
		} catch (Exception e) {
			m_logger.error("Failed to scrape {} - {}", bundleUrl, e.getMessage());
			return null;
		}
	}
	
	private List<Bundle> extractInfo(Map<?,?> bundleObj) {
		// WTF WTF WTF WTF
		List<Map<?,?>> bundles = (List<Map<?, ?>>) ((List<Map<?, ?>>) bundleObj.get("mosaic"))
				.get(0)
				.get("products");
		List<Bundle> output = new ArrayList<>();
		for (Map<?,?> bundle : bundles) {
			output.add(
					new Bundle(
							(String)bundle.get("tile_short_name"),
							(String)bundle.get("product_url")
							)
					);
		}
		return output;
	}

}
