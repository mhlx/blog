package me.qyh.blog.core.service;

import java.util.Collection;
import java.util.Map;
import java.util.OptionalInt;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.CommentStatistics;

public interface CommentServer {

	/**
	 * 查询某个模块下面的某项评论数目
	 * 
	 * @param module
	 * @param moduleId
	 * @return
	 */
	OptionalInt queryCommentNum(String module, Integer moduleId);

	/**
	 * 查詢某個模块下面某些项的评论数目
	 * 
	 * @param module
	 * @param moduleIds
	 * @return key 项目ID value 评论数目
	 */
	Map<Integer, Integer> queryCommentNums(String module, Collection<Integer> moduleIds);

	/**
	 * 查询某个空间下某个模块所有的评论
	 * 
	 * @param module
	 * @param space
	 * @param queryPrivate
	 *            是否查询私有模块项目的评论
	 * @return
	 */
	OptionalInt queryCommentNum(String module, Space space, boolean queryPrivate);

	/**
	 * 查询评论统计
	 * 
	 * @param space
	 *            空间，如果为空，查询所有空间
	 * @return
	 */
	CommentStatistics queryCommentStatistics(Space space);

	/**
	 * 
	 * 删除某个模块下的所有评论
	 * 
	 * @param module
	 * @param moduleId
	 * @since 6.6
	 */
	void deleteComments(String module, Integer moduleId);
}
