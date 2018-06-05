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
package me.qyh.blog.plugin.comment.vo;

import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.plugin.comment.entity.CommentMode;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;

public class CommentQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CommentStatus status;
	private CommentModule module;
	private boolean asc;
	private CommentMode mode;

	public CommentQueryParam() {
		super();
	}

	public CommentQueryParam(CommentQueryParam param) {
		this.status = param.status;
		this.mode = param.mode;
		this.module = param.module;
		this.asc = param.asc;
	}

	public CommentStatus getStatus() {
		return status;
	}

	public void setStatus(CommentStatus status) {
		this.status = status;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public CommentModule getModule() {
		return module;
	}

	public void setModule(CommentModule module) {
		this.module = module;
	}

	public CommentMode getMode() {
		return mode == null ? CommentMode.LIST : mode;
	}

	public void setMode(CommentMode mode) {
		this.mode = mode;
	}

	public boolean complete() {
		return module != null && module.getModule() != null && module.getId() != null;
	}

	@Override
	public String toString() {
		return "CommentQueryParam [status=" + status + ", module=" + module + ", asc=" + asc + ", mode=" + mode + "]";
	}
}
