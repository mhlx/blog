package me.qyh.blog.vo;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import me.qyh.blog.entity.Article.ArticleStatus;

public class ArticleQueryParam extends PageQueryParam {
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime end;
	private boolean queryPrivate;
	private boolean queryPasswordProtected;
	@Size(max = 20, message = "查询内容不能超过20个字符")
	private String query;
	private ArticleStatus status;
	private String category;
	private String tag;
	private boolean ignoreLevel;
	private boolean ignorePaging;

	public ArticleQueryParam() {
		super();
	}

	public ArticleQueryParam(ArticleQueryParam param) {
		super(param);
		this.begin = param.begin;
		this.end = param.end;
		this.queryPrivate = param.queryPrivate;
		this.queryPasswordProtected = param.queryPasswordProtected;
		this.query = param.query;
		this.status = param.status;
		this.category = param.category;
		this.tag = param.tag;
		this.ignoreLevel = param.ignoreLevel;
		this.ignorePaging = param.ignorePaging;
	}

	public LocalDateTime getBegin() {
		return begin;
	}

	public void setBegin(LocalDateTime begin) {
		this.begin = begin;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public boolean isQueryPrivate() {
		return queryPrivate;
	}

	public void setQueryPrivate(boolean queryPrivate) {
		this.queryPrivate = queryPrivate;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public ArticleStatus getStatus() {
		return status;
	}

	public void setStatus(ArticleStatus status) {
		this.status = status;
	}

	public boolean isQueryPasswordProtected() {
		return queryPasswordProtected;
	}

	public void setQueryPasswordProtected(boolean queryPasswordProtected) {
		this.queryPasswordProtected = queryPasswordProtected;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isIgnoreLevel() {
		return ignoreLevel;
	}

	public void setIgnoreLevel(boolean ignoreLevel) {
		this.ignoreLevel = ignoreLevel;
	}

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

}
