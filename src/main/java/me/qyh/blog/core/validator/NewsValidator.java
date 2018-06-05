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

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.util.Validators;

@Component
public class NewsValidator implements Validator {

	private static final int MAX_CONTENT_LENGTH = 2000;

	@Override
	public boolean supports(Class<?> clazz) {
		return News.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		News news = (News) target;
		String content = news.getContent();
		if (Validators.isEmptyOrNull(content, true)) {
			errors.reject("news.content.blank", "内容不能为空");
			return;
		}
		if (content.length() > MAX_CONTENT_LENGTH) {
			errors.reject("news.content.toolong", new Object[] { MAX_CONTENT_LENGTH },
					"内容不能超过" + MAX_CONTENT_LENGTH + "个字符");
			return;
		}
		if (news.getIsPrivate() == null) {
			errors.reject("news.private.blank", "是否私人不能为空");
			return;
		}
		if (news.getAllowComment() == null) {
			errors.reject("news.allowComment.blank", "是否允许评论不能为空");
			return;
		}
	}

}
