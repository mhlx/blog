package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.News;

public class NewsCreateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final News news;

	public NewsCreateEvent(Object source, News news) {
		super(source);
		this.news = news;
	}

	public News getNews() {
		return news;
	}

}
