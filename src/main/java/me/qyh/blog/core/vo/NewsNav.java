package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.News;

public class NewsNav {

	private final News previous;
	private final News next;

	public NewsNav(News previous, News next) {
		super();
		this.previous = previous;
		this.next = next;
	}

	public News getPrevious() {
		return previous;
	}

	public News getNext() {
		return next;
	}

}
