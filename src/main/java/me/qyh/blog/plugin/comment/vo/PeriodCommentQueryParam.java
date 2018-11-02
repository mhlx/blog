package me.qyh.blog.plugin.comment.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;

public class PeriodCommentQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date end;
	private CommentStatus status;

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

	public CommentStatus getStatus() {
		return status;
	}

	public void setStatus(CommentStatus status) {
		this.status = status;
	}

}
