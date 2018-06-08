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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.security.BCrypts;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.LoginBean;

@Service
public class UserServiceImpl implements UserService, InitializingBean {

	private static final Path USER_RES_PATH = Constants.CONFIG_DIR.resolve("user.properties");

	private static final EncodedResource USER_RES = new EncodedResource(new PathResource(USER_RES_PATH),
			Constants.CHARSET);
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String EMAIL = "email";

	/**
	 * admin，防止user.properties文件为空时自动登陆失败
	 */
	private final String DEFAULT_PASSWORD = "$2a$10$DZ/KQVvyKGQrI8rlRmE95uIBAPj6RcfThGTxXOhRDpFMA5zAvHeq.";

	private static final Properties pros;
	private User user;

	static {
		FileUtils.createFile(USER_RES_PATH);
		try {
			pros = PropertiesLoaderUtils.loadProperties(USER_RES);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Override
	public User getUser() {
		return new User(user);
	}

	@Override
	public User login(LoginBean loginBean) throws LogicException {
		if (user.getName().equals(loginBean.getUsername())
				&& BCrypts.matches(loginBean.getPassword(), user.getPassword())) {
			return getUser();
		}
		throw new LogicException("user.loginFail", "登录失败");
	}

	@Override
	public User update(User user, String password) throws LogicException {
		if (!BCrypts.matches(password, this.user.getPassword())) {
			throw new LogicException("user.update.errorPwd", "密码错误");
		}
		save(user);
		return getUser();
	}

	private synchronized void save(User user) {
		if (!Validators.isEmptyOrNull(user.getPassword(), true)) {
			pros.setProperty(PASSWORD, BCrypts.encode(user.getPassword()));
		}
		pros.setProperty(USERNAME, user.getName());
		pros.setProperty(EMAIL, user.getEmail() == null ? "" : user.getEmail());
		try (OutputStream os = new FileOutputStream(USER_RES.getResource().getFile())) {
			pros.store(os, "");
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		load();
	}

	private void load() {
		String username = pros.getProperty(USERNAME);
		String password = pros.getProperty(PASSWORD);
		String email = pros.getProperty(EMAIL, "");
		if (Validators.isEmptyOrNull(username, true)) {
			username = "admin";
		}
		if (Validators.isEmptyOrNull(password, true)) {
			password = DEFAULT_PASSWORD;
		}
		user = new User();
		user.setEmail(email);
		user.setName(username);
		user.setPassword(password);
		if (!Validators.isEmptyOrNull(email, true)) {
			user.setGravatar(DigestUtils.md5DigestAsHex(email.getBytes(Constants.CHARSET)));
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		load();
	}

}
