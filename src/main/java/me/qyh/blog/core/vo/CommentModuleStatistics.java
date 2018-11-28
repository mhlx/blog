package me.qyh.blog.core.vo;

import me.qyh.blog.core.message.Message;

public class CommentModuleStatistics {

	private final String type;
	private final Message name;
	private final int count;

	public CommentModuleStatistics(String type, Message name, int count) {
		super();
		this.type = type;
		this.name = name;
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public Message getName() {
		return name;
	}

	public int getCount() {
		return count;
	}

}
