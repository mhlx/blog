package me.qyh.blog.plugin.comment.vo;

import java.util.List;

import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.service.CommentConfig;

/**
 * 评论分页结果
 * 
 * @author Administrator
 *
 */
public final class CommentPageResult extends PageResult<Comment> {
	private final CommentConfig commentConfig;
	
	/**
	 * 审核中评论数目
	 * @since 6.0
	 */
	private int checkCount;

	public CommentPageResult(PageQueryParam param, int totalRow, List<Comment> datas, CommentConfig commentConfig) {
		super(param, totalRow, datas);
		this.commentConfig = commentConfig;
	}

	public CommentConfig getCommentConfig() {
		return commentConfig;
	}

	public int getCheckCount() {
		return checkCount;
	}

	public void setCheckCount(int checkCount) {
		this.checkCount = checkCount;
	}
}
