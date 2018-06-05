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
package me.qyh.blog.file.validator;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.file.vo.BlogFileQueryParam;


@Component
public class BlogFileQueryParamValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_EXTENSION_LENGTH = 5;

	@Override
	public boolean supports(Class<?> clazz) {
		return BlogFileQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		BlogFileQueryParam param = (BlogFileQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String name = param.getName();
		if (name != null && name.length() > MAX_NAME_LENGTH) {
			param.setName(name.substring(0, MAX_NAME_LENGTH));
		}
		// 最多只允许 5 个后缀
		param.setExtensions(param.getExtensions().stream().limit(MAX_EXTENSION_LENGTH).collect(Collectors.toSet()));
	}

}
