package me.qyh.blog.file.vo;

import java.util.Map;

import me.qyh.blog.core.message.Message;

public class BlogFileProperties {

	private final Map<String, Object> base;
	private final Map<Message, String> other;

	public BlogFileProperties(Map<String, Object> base, Map<Message, String> other) {
		super();
		this.base = base;
		this.other = other;
	}

	public BlogFileProperties(Map<String, Object> base) {
		this(base, null);
	}

	public Map<String, Object> getBase() {
		return base;
	}

	public Map<Message, String> getOther() {
		return other;
	}

}
