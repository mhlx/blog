package me.qyh.blog.vo;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class ArticleArchiveQueryParam extends PageQueryParam {
	private boolean queryPrivate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate end;
	private String category;

	public ArticleArchiveQueryParam() {
		super();
	}

	public ArticleArchiveQueryParam(ArticleArchiveQueryParam param) {
		super(param);
		this.queryPrivate = param.queryPrivate;
		this.begin = param.begin;
		this.end = param.end;
		this.category = param.category;
	}

	public boolean isQueryPrivate() {
		return queryPrivate;
	}

	public void setQueryPrivate(boolean queryPrivate) {
		this.queryPrivate = queryPrivate;
	}

	public LocalDate getBegin() {
		return begin;
	}

	public void setBegin(LocalDate begin) {
		this.begin = begin;
	}

	public LocalDate getEnd() {
		return end;
	}

	public void setEnd(LocalDate end) {
		this.end = end;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
