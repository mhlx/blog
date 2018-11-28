package me.qyh.blog.template.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.template.entity.Page;

public class PageUpdateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Page oldPage;
	private final Page newPage;

	public PageUpdateEvent(Object source, Page oldPage, Page newPage) {
		super(source);
		this.oldPage = oldPage;
		this.newPage = newPage;
	}

	public Page getOldPage() {
		return oldPage;
	}

	public Page getNewPage() {
		return newPage;
	}

}
