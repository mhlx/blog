package me.qyh.blog.plugin.qiniu;

public class QiniuConfig {

	private String accessKey;
	private String secretKey;

	private Integer smallSize;
	private Integer middleSize;
	private Integer largeSize;

	private boolean readonly;

	private String urlPrefix;// 外链域名

	private Character styleSplitChar;
	private boolean sourceProtected;// 原图保护
	private String style;// 样式
	private String bucket;

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public Integer getSmallSize() {
		return smallSize;
	}

	public void setSmallSize(Integer smallSize) {
		this.smallSize = smallSize;
	}

	public Integer getMiddleSize() {
		return middleSize;
	}

	public void setMiddleSize(Integer middleSize) {
		this.middleSize = middleSize;
	}

	public Integer getLargeSize() {
		return largeSize;
	}

	public void setLargeSize(Integer largeSize) {
		this.largeSize = largeSize;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	public Character getStyleSplitChar() {
		return styleSplitChar;
	}

	public void setStyleSplitChar(Character styleSplitChar) {
		this.styleSplitChar = styleSplitChar;
	}

	public boolean isSourceProtected() {
		return sourceProtected;
	}

	public void setSourceProtected(boolean sourceProtected) {
		this.sourceProtected = sourceProtected;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

}
