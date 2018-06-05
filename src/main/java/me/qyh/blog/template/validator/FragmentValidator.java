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
package me.qyh.blog.template.validator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.entity.Fragment;

@Component
public class FragmentValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;
	public static final int MAX_TPL_LENGTH = 20000;
	private static final int MAX_DESCRIPTION_LENGTH = 500;

	private static final String NAME_PATTERN = "^[A-Za-z0-9\u4E00-\u9FA5_-]+$";

	@Override
	public boolean supports(Class<?> clazz) {
		return Fragment.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Fragment fragment = (Fragment) target;
		String name = fragment.getName();
		try {
			name = validName(name, false);
		} catch (LogicException e) {
			Message msg = e.getLogicMessage();
			errors.reject(msg.getCodes()[0], msg.getArguments(), msg.getDefaultMessage());
			return;
		}
		String tpl = fragment.getTpl();
		if (Validators.isEmptyOrNull(tpl, true)) {
			errors.reject("fragment.tpl.null", "模板片段模板不能为空");
			return;
		}
		if (tpl.length() > MAX_TPL_LENGTH) {
			errors.reject("fragment.tpl.toolong", new Object[] { MAX_TPL_LENGTH },
					"模板片段模板长度不能超过" + MAX_TPL_LENGTH + "个字符");
			return;
		}
		String description = fragment.getDescription();
		if (description == null) {
			errors.reject("fragment.description.null", "模板片段描述不能为空");
			return;
		}
		if (description.length() > MAX_DESCRIPTION_LENGTH) {
			errors.reject("fragment.description.toolong", new Object[] { MAX_DESCRIPTION_LENGTH },
					"模板片段描述长度不能超过" + MAX_DESCRIPTION_LENGTH + "个字符");
		}
	}

	public static String validName(String name, boolean encode) throws LogicException {
		if (Validators.isEmptyOrNull(name, true)) {
			throw new LogicException("fragment.name.blank", "模板片段名为空");
		}
		if (encode) {
			try {
				name = URLDecoder.decode(name, Constants.CHARSET.name());
			} catch (UnsupportedEncodingException e) {
				throw new LogicException("fragment.name.undecode", "无法解码的模板片段名称");
			}
		}
		if (name.length() > MAX_NAME_LENGTH) {
			throw new LogicException("fragment.name.toolong", "模板片段名长度不能超过" + MAX_NAME_LENGTH + "个字符", MAX_NAME_LENGTH);
		}
		if (!name.matches(NAME_PATTERN)) {
			throw new LogicException("fragment.name.invalid", "无效的模板片段名称");
		}
		return name;
	}

}
