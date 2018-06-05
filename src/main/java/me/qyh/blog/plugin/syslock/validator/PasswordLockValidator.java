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
package me.qyh.blog.plugin.syslock.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.syslock.entity.PasswordLock;

@Component
public class PasswordLockValidator extends SysLockValidator {

	private static final int MAX_PASSWORD_LENGTH = 16;

	@Override
	public boolean supports(Class<?> clazz) {
		return PasswordLock.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		super.validate(target, errors);
		if (errors.hasErrors()) {
			return;
		}
		PasswordLock lock = (PasswordLock) target;
		String password = lock.getPassword();
		if (Validators.isEmptyOrNull(password, true)) {
			errors.reject("lock.pwd.empty", "锁的密码不能为空");
			return;
		}
		if (password.length() > MAX_PASSWORD_LENGTH) {
			errors.reject("lock.pwd.toolong", "锁的密码不能超过" + MAX_PASSWORD_LENGTH + "个字符");
		}
	}
}