package me.qyh.blog.core.vo;

import java.util.List;

import me.qyh.blog.core.entity.Article;

public class ArticleArchive {

	private final String ymd;
	private final List<Article> articles;

	public ArticleArchive(String ymd, List<Article> articles) {
		super();
		this.ymd = ymd;
		this.articles = articles;
	}

	public String getYmd() {
		return ymd;
	}

	public List<Article> getArticles() {
		return articles;
	}

}
