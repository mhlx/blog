package me.qyh.blog.core.plugin;

import me.qyh.blog.core.message.Message;

public class Icon {

	private final Message name;
	private final String icon;
	private final String relativeUrl;

	public Icon(Message name, String icon, String relativeUrl) {
		super();
		this.name = name;
		this.icon = icon;
		this.relativeUrl = relativeUrl;
	}

	public Message getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}

	public String getRelativeUrl() {
		return relativeUrl;
	}

}
