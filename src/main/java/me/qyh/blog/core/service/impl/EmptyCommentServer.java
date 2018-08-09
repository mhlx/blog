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
package me.qyh.blog.core.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.vo.CommentStatistics;

public class EmptyCommentServer implements CommentServer {

	private EmptyCommentServer() {
		super();
	}

	public static final EmptyCommentServer INSTANCE = new EmptyCommentServer();

	@Override
	public Map<Integer, Integer> queryCommentNums(String module, Collection<Integer> moduleIds) {
		return new HashMap<>();
	}

	@Override
	public OptionalInt queryCommentNum(String module, Space space, boolean queryPrivate) {
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt queryCommentNum(String module, Integer moduleId) {
		return OptionalInt.empty();
	}

	@Override
	public CommentStatistics queryCommentStatistics(Space space) {
		return new CommentStatistics();
	}

	@Override
	public void deleteComments(String module, Integer moduleId) {

	}

}
