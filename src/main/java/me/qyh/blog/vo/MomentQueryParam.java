package me.qyh.blog.vo;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class MomentQueryParam extends PageQueryParam {
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime end;
	private boolean queryPrivate;
	private boolean asc;
	@Size(max = 20, message = "动态查询内容不能超过20个字符")
	private String query;
	private boolean ignorePaging;
	private boolean queryPasswordProtected;

	public MomentQueryParam() {
		super();
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

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
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

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

	public boolean isQueryPasswordProtected() {
		return queryPasswordProtected;
	}

	public void setQueryPasswordProtected(boolean queryPasswordProtected) {
		this.queryPasswordProtected = queryPasswordProtected;
	}

	@Override
	public String toString() {
		return "MomentQueryParam [begin=" + begin + ", end=" + end + ", queryPrivate=" + queryPrivate + ", asc=" + asc
				+ ", query=" + query + ", ignorePaging=" + ignorePaging + ", queryPasswordProtected="
				+ queryPasswordProtected + "]";
	}

}
