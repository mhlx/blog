package me.qyh.blog.plugin.sitemap;

public class SiteUrl {

	private String loc;
	private String lastmod;
	private String changefreq;
	private Double priority;

	public SiteUrl() {
		super();
	}

	public SiteUrl(String loc) {
		super();
		this.loc = loc;
	}

	public SiteUrl(String loc, String lastmod, String changefreq) {
		super();
		this.loc = loc;
		this.lastmod = lastmod;
		this.changefreq = changefreq;
	}

	public SiteUrl(String loc, Double priority) {
		super();
		this.loc = loc;
		this.priority = priority;
	}

	public SiteUrl(String loc, String lastmod, String changefreq, Double priority) {
		super();
		this.loc = loc;
		this.lastmod = lastmod;
		this.changefreq = changefreq;
		this.priority = priority;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public String getLastmod() {
		return lastmod;
	}

	public void setLastmod(String lastmod) {
		this.lastmod = lastmod;
	}

	public String getChangefreq() {
		return changefreq;
	}

	public void setChangefreq(String changefreq) {
		this.changefreq = changefreq;
	}

	public Double getPriority() {
		return priority;
	}

	public void setPriority(Double priority) {
		this.priority = priority;
	}
}
