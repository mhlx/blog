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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.vo.NewsArchivePageQueryParam;

/**
 * 
 * @author Administrator
 *
 */
@Component
public class NewsArchivePageQueryParamValidator implements Validator {

	private static final int MAX_CONTENT_LENGTH = 50;

	@Override
	public boolean supports(Class<?> clazz) {
		return NewsArchivePageQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		NewsArchivePageQueryParam param = (NewsArchivePageQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String ymd = param.getYmd();
		if (ymd != null) {
			try {
				LocalDate.parse(ymd);
			} catch (DateTimeParseException e) {
				param.setYmd(null);
			}
		}
		String content = param.getContent();
		if (content != null && content.length() > MAX_CONTENT_LENGTH) {
			param.setContent(content.substring(0, MAX_CONTENT_LENGTH));
		}
	}

}
