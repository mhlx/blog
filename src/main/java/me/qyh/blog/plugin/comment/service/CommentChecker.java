package me.qyh.blog.plugin.comment.service;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.comment.entity.Comment;

/**
 * 在插入评论前检查评论
 * 
 * @author Administrator
 *
 */
public interface CommentChecker {

	/**
	 * 校验评论人昵称、邮箱以及内容、网址等是否被允许
	 * 
	 * @param comment
	 *            评论
	 * @throws LogicException
	 */
	void checkComment(Comment comment, CommentConfig config) throws LogicException;

}
