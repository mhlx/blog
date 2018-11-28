package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.News;

public class NewsUpdateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final News oldNews;
	private final News newNews;

	public NewsUpdateEvent(Object source, News oldNews, News newNews) {
		super(source);
		this.oldNews = oldNews;
		this.newNews = newNews;
	}

	public News getOldNews() {
		return oldNews;
	}

	public News getNewNews() {
		return newNews;
	}

}
