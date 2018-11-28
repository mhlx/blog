package me.qyh.blog.template.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.template.entity.Page;

public class PageDelEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Page> pages;

	public PageDelEvent(Object source, List<Page> pages) {
		super(source);
		this.pages = pages;
	}

	public List<Page> getPages() {
		return pages;
	}

}
