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
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;

/**
 * 用于禁止某些IP评论
 * 
 * @since 6.0
 * @author wwwqyhme
 *
 */
public interface BlacklistHandler {

	/**
	 * 分页查询黑名单中的IP
	 * 
	 * @param param
	 * @return
	 */
	PageResult<String> query(IPQueryParam param);

	/**
	 * 将IP从黑名单中移除
	 * 
	 * @param ip
	 */
	void remove(String ip);

	/**
	 * 将IP加入黑名单
	 * 
	 * @param ip
	 * @throws LogicException
	 */
	void add(String ip) throws LogicException;

	/**
	 * 判断IP是否在黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	boolean match(String ip);
}
