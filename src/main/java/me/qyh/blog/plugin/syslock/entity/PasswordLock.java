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
package me.qyh.blog.plugin.syslock.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.annotations.Expose;

import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.BCrypts;
import me.qyh.blog.core.util.Validators;

/**
 * 密码锁
 * 
 * @author Administrator
 *
 */
public class PasswordLock extends SysLock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String PASSWORD_PARAMETER = "password";

	@Expose(serialize = false)
	private String password;

	/**
	 * default
	 */
	public PasswordLock() {
		super(SysLockType.PASSWORD);
	}

	@Override
	public LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException {
		final String requestPassword = request.getParameter(PASSWORD_PARAMETER);
		if (Validators.isEmptyOrNull(requestPassword, true)) {
			throw new LogicException(new Message("lock.password.password.blank", "密码不能为空"));
		}
		return new LockKey() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String lockId() {
				return getId();
			}

			@Override
			public Serializable getKey() {
				return requestPassword;
			}
		};
	}

	@Override
	public void tryOpen(LockKey key) throws LogicException {
		Objects.requireNonNull(key);
		Object keyData = key.getKey();
		if (BCrypts.matches(keyData.toString(), password)) {
			return;
		}
		throw new LogicException(new Message("lock.password.unlock.fail", "密码验证失败"));
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
