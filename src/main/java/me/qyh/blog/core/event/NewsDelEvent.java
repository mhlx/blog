package me.qyh.blog.core.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.News;

public class NewsDelEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<News> newsList;

	public NewsDelEvent(Object source, List<News> newsList) {
		super(source);
		this.newsList = newsList;
	}

	public List<News> getNewsList() {
		return newsList;
	}

}
