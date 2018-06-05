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
package me.qyh.blog.template.render.data;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.CommentServer;

public class CommentCountDataTagProcessor extends DataTagProcessor<Integer> {

	@Autowired(required = false)
	private CommentServer commentServer;

	public CommentCountDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected Integer query(Attributes attributes) throws LogicException {
		if (commentServer == null) {
			return 0;
		}
		String moduleType = attributes.getString("moduleType").orElse(null);
		String moduleId = attributes.getString("moduleId").orElse(null);
		if (moduleType != null && moduleId != null) {
			try {
				return commentServer.queryCommentNum(moduleType, Integer.parseInt(moduleId)).orElse(0);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 0;
	}

}
