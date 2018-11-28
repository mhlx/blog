package me.qyh.blog.plugin.comment.vo;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.plugin.comment.entity.Comment;

public class LastNewsComment extends Comment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private News news;

	public News getNews() {
		return news;
	}

	public void setNews(News news) {
		this.news = news;
	}

}
