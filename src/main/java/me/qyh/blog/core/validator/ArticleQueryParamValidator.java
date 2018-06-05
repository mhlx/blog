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

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.vo.ArticleQueryParam;


/**
 * 
 * @author Administrator
 *
 */
@Component
public class ArticleQueryParamValidator implements Validator {

	private static final int MAX_QUERY_LENGTH = 40;
	private static final int MAX_TAG_LENGTH = 20;

	private static final int MAX_SPACES_ALIAS_SIZE = 10;

	@Override
	public boolean supports(Class<?> clazz) {
		return ArticleQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ArticleQueryParam param = (ArticleQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String query = param.getQuery();
		if (query != null && query.length() > MAX_QUERY_LENGTH) {
			param.setQuery(query.substring(0, MAX_QUERY_LENGTH));
		}
		String tag = param.getTag();
		if (tag != null && tag.length() > MAX_TAG_LENGTH) {
			param.setTag(tag.substring(0, MAX_TAG_LENGTH));
		}
		Date begin = param.getBegin();
		Date end = param.getEnd();
		if (begin != null && end != null && begin.after(end)) {
			param.setBegin(null);
			param.setEnd(null);
		}
		param.setSpaces(param.getSpaces().stream().limit(MAX_SPACES_ALIAS_SIZE).collect(Collectors.toSet()));
	}

}
