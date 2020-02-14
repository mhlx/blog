package me.qyh.blog.vo;

import me.qyh.blog.entity.Article.ArticleStatus;

public class ArticleStatusStatistic {

	private ArticleStatus status;
	private int count;

	public ArticleStatus getStatus() {
		return status;
	}

	public void setStatus(ArticleStatus status) {
		this.status = status;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
