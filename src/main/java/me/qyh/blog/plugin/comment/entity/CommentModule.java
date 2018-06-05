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
package me.qyh.blog.plugin.comment.entity;

import java.io.Serializable;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

/**
 * 评论区域
 * 
 * @author Administrator
 *
 */
public class CommentModule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @since 5.6
	 */
	private String module;
	private Integer id;// 关联id

	/**
	 * @since 5.5.5
	 */
	private Object object;// 关联对象

	public CommentModule(String module, Integer id) {
		super();
		this.module = module;
		this.id = id;
	}

	public CommentModule() {
		super();
	}

	/**
	 * @return the module
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(String module) {
		this.module = module;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	@Override
	public int hashCode() {
		return Objects.hash(module, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			CommentModule rhs = (CommentModule) obj;
			return Objects.equals(this.module, rhs.module) && Objects.equals(this.id, rhs.id);
		}
		return false;
	}

}
