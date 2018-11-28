package me.qyh.blog.core.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class NewsQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date end;
	private boolean queryPrivate;
	private boolean asc;
	private String content;
	private boolean queryLock = true;
	private boolean ignorePaging;

	public NewsQueryParam() {
		super();
	}

	public NewsQueryParam(NewsQueryParam param) {
		super(param);
		this.begin = param.begin;
		this.end = param.end;
		this.queryPrivate = param.queryPrivate;
		this.asc = param.asc;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean isQueryPrivate() {
		return queryPrivate;
	}

	public void setQueryPrivate(boolean queryPrivate) {
		this.queryPrivate = queryPrivate;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isQueryLock() {
		return queryLock;
	}

	public void setQueryLock(boolean queryLock) {
		this.queryLock = queryLock;
	}

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

}
