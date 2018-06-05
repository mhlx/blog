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
package me.qyh.blog.core.entity;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.exception.LogicException;

/**
 * 锁，如果用户为对象设置了锁，那么访问的时候需要解锁才能访问(非登录用户)
 * 
 * @author Administrator
 *
 */
public abstract class Lock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;

	/**
	 * 从请求中获取钥匙
	 * 
	 * @param request
	 *            当前请求
	 */
	public abstract LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException;

	/**
	 * 开锁
	 * 
	 * @param key
	 *            钥匙
	 */
	public abstract void tryOpen(LockKey key) throws LogicException;

	/**
	 * 获取锁类型(用于模板)
	 * 
	 * @return 锁类型
	 */
	public abstract String getLockType();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
