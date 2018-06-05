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
package me.qyh.blog.core.service;

import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.LoginBean;

public interface UserService {

	/**
	 * 登录
	 * 
	 * @param loginBean
	 * @return
	 * @throws LogicException
	 */
	User login(LoginBean loginBean) throws LogicException;

	/**
	 * 更新用户
	 * 
	 * @param user
	 *            当前用户
	 * @param password
	 *            密码
	 * @return
	 * @throws LogicException
	 */
	User update(User user, String password) throws LogicException;

	/**
	 * 获取管理員信息
	 * 
	 * @return
	 */
	User getUser();

}
