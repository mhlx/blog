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
