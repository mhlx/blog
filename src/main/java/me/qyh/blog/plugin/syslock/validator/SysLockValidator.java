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

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.syslock.entity.SysLock;

public abstract class SysLockValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;

	@Override
	public void validate(Object target, Errors errors) {
		SysLock lock = (SysLock) target;
		String name = lock.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("lock.name.empty", "锁的名称不能为空");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("lock.name.toolong", "锁的名称不能超过" + MAX_NAME_LENGTH + "个字符");
			return;
		}
	}
}