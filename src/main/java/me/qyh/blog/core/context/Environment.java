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
package me.qyh.blog.core.context;

import java.util.Objects;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.security.AuthencationException;

/**
 * 当前环境，用于在上下文中获取用户、空间以及当前ip信息
 * 
 * @author Administrator
 *
 */
public final class Environment {

	private static final ThreadLocal<Env> LOCAL = ThreadLocal.withInitial(Env::new);

	/**
	 * 获取当前用户
	 * 
	 * @return 如果没有登录，返回null
	 */
	public static User getUser() {
		return LOCAL.get().user;
	}

	/**
	 * 获取当前空间
	 * 
	 * @return 如果没有访问任何空间，返回null
	 * 
	 */
	public static Space getSpace() {
		return LOCAL.get().space;
	}

	/**
	 * 验证当前用户是否已经登录
	 * 
	 */
	public static void doAuthencation() {
		if (!hasAuthencated()) {
			throw new AuthencationException();
		}
	}

	/**
	 * 目标空间是否匹配当前空间<br>
	 * <b>如果两个空间id相同或者拥有相同的别名，则认为匹配</b>
	 * 
	 * @param space
	 *            目标空间 ，可以为null
	 * @return
	 */
	public static boolean match(Space space) {
		Space current = getSpace();
		return Objects.equals(current, space)
				|| (space != null && current != null && Objects.equals(current.getAlias(), space.getAlias()));
	}

	/**
	 * 设置用户上下文
	 * 
	 * @param user
	 *            用户
	 */
	public static void setUser(User user) {
		LOCAL.get().user = user;
	}

	/**
	 * 判断用户是否已经登录
	 * 
	 * @return
	 */
	public static boolean hasAuthencated() {
		return getUser() != null;
	}

	/**
	 * 设置空间上下文
	 * 
	 * @param space
	 */
	public static void setSpace(Space space) {
		LOCAL.get().space = space;
	}

	/**
	 * 是否处于空间中
	 * 
	 * @return
	 */
	public static boolean hasSpace() {
		return getSpace() != null;
	}

	/**
	 * 获取当前空间别名
	 * 
	 * @return
	 */
	public static String getSpaceAlias() {
		Space space = getSpace();
		return space == null ? null : space.getAlias();
	}

	/**
	 * 获取当前访问的IP
	 * 
	 * @return
	 */
	public static String getIP() {
		return LOCAL.get().ip;
	}

	/**
	 * 设置当前访问IP
	 * 
	 * @param ip
	 */
	public static void setIP(String ip) {
		LOCAL.get().ip = ip;
	}

	/**
	 * 设置当前访问IP是否为预览IP
	 * 
	 * @param ip
	 */
	public static void setPreview(boolean preview) {
		LOCAL.get().preview = preview;
	}

	/**
	 * 判断当前请求IP是否为预览IP
	 * 
	 * @return
	 */
	public static boolean isPreview() {
		return LOCAL.get().preview;
	}

	/**
	 * 清空所有的上下文
	 */
	public static void remove() {
		LOCAL.remove();
	}

	private static final class Env {
		private User user;
		private Space space;
		private String ip;
		private boolean preview;
	}
}
