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

import me.qyh.blog.core.config.GlobalConfig;

@Component
public class GlobalConfigValidator implements Validator {

	private static final int[] FILE_PAGE_SIZE_RANGE = { 1, 50 };
	private static final int[] USER_FRAGEMENT_PAGE_SIZE_RANGE = { 1, 100 };
	private static final int[] USER_PAGE_PAGE_SIZE_RANGE = { 1, 100 };
	private static final int[] ARTICLE_PAGE_SIZE_RANGE = { 1, 50 };
	private static final int[] TAG_PAGE_SIZE_RANGE = { 1, 50 };
	private static final int[] NEWS_PAGE_SIZE_RANGE = { 1, 50 };

	@Override
	public boolean supports(Class<?> clazz) {
		return GlobalConfig.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		GlobalConfig config = (GlobalConfig) target;
		int filePageSize = config.getFilePageSize();
		if (filePageSize < FILE_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.file.toosmall", new Object[] { FILE_PAGE_SIZE_RANGE[0] },
					"文件每页数量不能小于" + FILE_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (filePageSize > FILE_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.file.toobig", new Object[] { FILE_PAGE_SIZE_RANGE[1] },
					"文件每页数量不能大于" + FILE_PAGE_SIZE_RANGE[1]);
			return;
		}
		int userWidgetPageSize = config.getFragmentPageSize();
		if (userWidgetPageSize < USER_FRAGEMENT_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.userWidget.toosmall", new Object[] { USER_FRAGEMENT_PAGE_SIZE_RANGE[0] },
					"用户挂件每页数量不能小于" + USER_FRAGEMENT_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (userWidgetPageSize > USER_FRAGEMENT_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.userWidget.toobig", new Object[] { USER_FRAGEMENT_PAGE_SIZE_RANGE[1] },
					"用户挂件每页数量不能大于" + USER_FRAGEMENT_PAGE_SIZE_RANGE[1]);
			return;
		}

		int pagePageSize = config.getPagePageSize();
		if (pagePageSize < USER_PAGE_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.page.toosmall", new Object[] { USER_PAGE_PAGE_SIZE_RANGE[0] },
					"用户自定义页面每页数量不能小于" + USER_PAGE_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (pagePageSize > USER_PAGE_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.page.toobig", new Object[] { USER_PAGE_PAGE_SIZE_RANGE[1] },
					"用户自定义页面每页数量不能大于" + USER_PAGE_PAGE_SIZE_RANGE[1]);
			return;
		}

		int articlePageSize = config.getArticlePageSize();
		if (articlePageSize < ARTICLE_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.article.toosmall", new Object[] { ARTICLE_PAGE_SIZE_RANGE[0] },
					"文章每页数量不能小于" + ARTICLE_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (articlePageSize > ARTICLE_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.article.toobig", new Object[] { ARTICLE_PAGE_SIZE_RANGE[1] },
					"文章每页数量不能大于" + ARTICLE_PAGE_SIZE_RANGE[1]);
			return;
		}

		int tagPageSize = config.getTagPageSize();
		if (tagPageSize < TAG_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.tag.toosmall", new Object[] { TAG_PAGE_SIZE_RANGE[0] },
					"标签每页数量不能小于" + TAG_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (tagPageSize > TAG_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.tag.toobig", new Object[] { TAG_PAGE_SIZE_RANGE[1] },
					"标签每页数量不能大于" + TAG_PAGE_SIZE_RANGE[1]);
		}

		int newsPageSize = config.getNewsPageSize();
		if (newsPageSize < NEWS_PAGE_SIZE_RANGE[0]) {
			errors.reject("global.pagesize.news.toosmall", new Object[] { NEWS_PAGE_SIZE_RANGE[0] },
					"动态每页数量不能小于" + NEWS_PAGE_SIZE_RANGE[0]);
			return;
		}

		if (newsPageSize > NEWS_PAGE_SIZE_RANGE[1]) {
			errors.reject("global.pagesize.news.toobig", new Object[] { NEWS_PAGE_SIZE_RANGE[1] },
					"动态每页数量不能大于" + NEWS_PAGE_SIZE_RANGE[1]);
		}
	}

}
