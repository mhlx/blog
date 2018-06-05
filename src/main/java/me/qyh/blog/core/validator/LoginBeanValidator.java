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
package me.qyh.blog.core.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.LoginBean;

@Component
public class LoginBeanValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return LoginBean.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		LoginBean loginBean = (LoginBean) target;
		String username = loginBean.getUsername();
		if (Validators.isEmptyOrNull(username, true)) {
			errors.reject("login.username.blank", "用户名不能为空");
			return;
		}
		if (username.length() > UserValidator.MAX_NAME_LENGTH) {
			errors.reject("login.username.valid", "无效的用户名");
			return;
		}
		String password = loginBean.getPassword();
		if (Validators.isEmptyOrNull(password, true)) {
			errors.reject("login.password.blank", "密码不能为空");
			return;
		}
		if (password.length() > UserValidator.MAX_PWD_LENGTH) {
			errors.reject("login.password.valid", "无效的密码");
		}
	}

}
