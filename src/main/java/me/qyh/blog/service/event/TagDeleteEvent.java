package me.qyh.blog.service.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.entity.Tag;

public class TagDeleteEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Tag tag;

	public TagDeleteEvent(Object source, Tag tag) {
		super(source);
		this.tag = tag;
	}

	public Tag getTag() {
		return tag;
	}

}
