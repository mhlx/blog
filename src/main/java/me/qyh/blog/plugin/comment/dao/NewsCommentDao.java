package me.qyh.blog.plugin.comment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.plugin.comment.entity.Comment;

public interface NewsCommentDao {
	List<Comment> selectLastComments(@Param("limit") int limit, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryAdmin") boolean queryAdmin);

	int selectTotalCommentCount(@Param("queryPrivate") boolean queryPrivate);

}
