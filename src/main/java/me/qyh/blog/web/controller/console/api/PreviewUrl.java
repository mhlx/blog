package me.qyh.blog.web.controller.console.api;

final class PreviewUrl {

	private final String url;
	private final boolean hasPathVariable;

	public PreviewUrl(String url, boolean hasPathVariable) {
		super();
		this.url = url;
		this.hasPathVariable = hasPathVariable;
	}

	public String getUrl() {
		return url;
	}

	public boolean isHasPathVariable() {
		return hasPathVariable;
	}

}
