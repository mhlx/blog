/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
