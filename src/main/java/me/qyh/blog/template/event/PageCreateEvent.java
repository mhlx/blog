package me.qyh.blog.template.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.template.entity.Page;

public class PageCreateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Page page;

	public PageCreateEvent(Object source, Page page) {
		super(source);
		this.page = page;
	}

	public Page getPage() {
		return page;
	}

}
