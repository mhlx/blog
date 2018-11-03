package me.qyh.blog.core.vo;

import java.util.List;

import me.qyh.blog.core.entity.News;

public class NewsArchive {

	private final String ymd;
	private final List<News> newses;

	public NewsArchive(String ymd, List<News> newses) {
		super();
		this.ymd = ymd;
		this.newses = newses;
	}

	public String getYmd() {
		return ymd;
	}

	public List<News> getNewses() {
		return newses;

	}

}
