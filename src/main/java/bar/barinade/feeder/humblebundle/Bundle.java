package bar.barinade.feeder.humblebundle;

public class Bundle {
	private String name;
	private String url;
	public Bundle(String name, String url) {
		this.name = name;
		this.url = url;
	}
	public String name() {
		return name;
	}
	public String url() {
		return url;
	}
}
