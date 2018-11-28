package me.qyh.blog.plugin.comment;

class CommentConfig {

	private final boolean enableEmailNotify;
	private EmailNofityConfig config;

	CommentConfig(boolean enableEmailNotify) {
		super();
		this.enableEmailNotify = enableEmailNotify;
	}

	public boolean isEnableEmailNotify() {
		return enableEmailNotify;
	}

	public EmailNofityConfig getConfig() {
		return config;
	}

	public void setConfig(EmailNofityConfig config) {
		this.config = config;
	}

}
