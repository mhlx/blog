package me.qyh.blog.template.render;

public class ParseConfig {
	private final boolean onlyCallable;

	/**
	 * @since 6.5
	 */
	private String contentType;

	public ParseConfig(boolean onlyCallable) {
		super();
		this.onlyCallable = onlyCallable;
	}

	public ParseConfig(boolean onlyCallable, String contentType) {
		super();
		this.onlyCallable = onlyCallable;
		this.contentType = contentType;
	}

	public ParseConfig() {
		this(false);
	}

	public boolean isOnlyCallable() {
		return onlyCallable;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
