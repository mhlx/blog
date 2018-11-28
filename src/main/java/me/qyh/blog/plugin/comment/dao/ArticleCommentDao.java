package me.qyh.blog.plugin.comment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.plugin.comment.entity.Comment;

public interface ArticleCommentDao {
	
	/**
	 * 查询文章的最后几条评论
	 * 
	 * @param space
	 *            空间
	 * @param limit
	 *            总数
	 * @param queryPrivate
	 *            是否查询私有空间|文章下的评论
	 * @return 评论集
	 */
	List<Comment> selectLastComments(@Param("space") Space space,
			@Param("limit") int limit, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryAdmin") boolean queryAdmin);
	
	/**
	 * 查询某个类型下的某个空间的评论总数
	 *
	 * @param space
	 *            空间，如果为空，查询全部
	 * @param queryPrivate
	 *            是否查询私有空间|文章
	 * @return
	 */
	int selectTotalCommentCount(@Param("space") Space space,
			@Param("queryPrivate") boolean queryPrivate);
}
