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
package me.qyh.blog.plugin.comment.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;
import me.qyh.blog.plugin.comment.entity.CommentMode;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.vo.CommentPageResult;
import me.qyh.blog.plugin.comment.vo.CommentQueryParam;
import me.qyh.blog.template.render.data.DataTagProcessor;

public class CommentsDataTagProcessor extends DataTagProcessor<CommentPageResult> {
	@Autowired
	private CommentService commentService;

	public CommentsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected CommentPageResult query(Attributes attributes) throws LogicException {
		CommentQueryParam param = new CommentQueryParam();

		String moduleTypeStr = attributes.getString("moduleType").orElse(null);
		String moduleIdStr = attributes.getString("moduleId").orElse(null);
		if (moduleIdStr != null && moduleTypeStr != null) {
			try {
				param.setModule(new CommentModule(moduleTypeStr, Integer.parseInt(moduleIdStr)));
			} catch (Exception e) {
				LOGGER.debug(e.getMessage(), e);
			}
		}

		param.setMode(attributes.getEnum("mode", CommentMode.class).orElse(CommentMode.LIST));
		param.setAsc(attributes.getBoolean("asc").orElse(true));
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(0));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));

		if (param.getCurrentPage() < 0) {
			param.setCurrentPage(0);
		}

		int pageSize = commentService.getCommentConfig().getPageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		param.setStatus(!Environment.hasAuthencated() ? CommentStatus.NORMAL : null);

		return commentService.queryComment(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("moduleType", "moduleId", "mode", "asc", "currentPage", "pageSize");
	}

}
