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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Validators;

@Component
public class ArticleValidator implements Validator {

	private static final int MAX_SUMMARY_LENGTH = 2000;
	private static final int MAX_TITLE_LENGTH = 200;
	private static final int MAX_ALIAS_LENGTH = 200;
	private static final int MAX_CONTENT_LENGTH = 200000;
	private static final int MAX_TAG_SIZE = 10;
	private static final int[] LEVEL_RANGE = new int[] { 0, 100 };

	/**
	 * 特征图像最大长度
	 * 
	 * @since 5.5.3
	 */
	private static final int MAX_FEATURE_IMAGE_SIZE = 255;

	@Autowired
	private TagValidator tagValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		return Article.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Article article = (Article) target;
		String title = article.getTitle();
		if (Validators.isEmptyOrNull(title, true)) {
			errors.reject("article.title.blank", "文章标题不能为空");
			return;
		}
		title = Jsoup.clean(title, Whitelist.none());
		if (title.strip().isEmpty()) {
			errors.reject("article.title.blank", "文章标题不能为空");
			return;
		}
		article.setTitle(title);
		if (title.length() > MAX_TITLE_LENGTH) {
			errors.reject("article.title.toolong", new Object[] { MAX_TITLE_LENGTH },
					"文章标题不能超过" + MAX_TITLE_LENGTH + "个字符");
			return;
		}
		Set<Tag> tags = article.getTags();
		if (!CollectionUtils.isEmpty(tags)) {
			if (tags.size() > MAX_TAG_SIZE) {
				errors.reject("article.tags.oversize", new Object[] { MAX_TAG_SIZE }, "文章标签不能超过" + MAX_TAG_SIZE + "个");
				return;
			}
			for (Tag tag : tags) {
				tagValidator.validate(tag, errors);
				if (errors.hasErrors()) {
					return;
				}
			}
		}
		if (article.getAllowComment() == null) {
			errors.reject("article.allowComment.null", "文章是否允许评论不能为空");
			return;
		}
		if (article.getIsPrivate() == null) {
			errors.reject("article.private.null", "文章私有性不能为空");
			return;
		}
		if (article.getEditor() == null) {
			errors.reject("article.editor.null", "文章编辑器不能为空");
			return;
		}
		if (article.getFrom() == null) {
			errors.reject("article.from.null", "文章标来源不能为空");
			return;
		}
		ArticleStatus status = article.getStatus();
		if (status == null) {
			errors.reject("article.status.null", "文章状态不能为空");
			return;
		}
		if (article.isDeleted()) {
			errors.reject("article.status.invalid", "无效的文章状态");
			return;
		}
		if (article.isSchedule()) {
			Date pubDate = article.getPubDate();
			if (pubDate == null) {
				errors.reject("article.pubDate.null", "文章发表日期不能为空");
				return;
			}
			if (pubDate.before(new Date())) {
				errors.reject("article.pubDate.toosmall", "文章发表日期不能小于当前日期");
				return;
			}
		}
		Space space = article.getSpace();
		if (space == null || !space.hasId()) {
			errors.reject("article.space.null", "文章所属空间不能为空");
			return;
		}
		Integer level = article.getLevel();
		if (level != null && (level < LEVEL_RANGE[0] || level > LEVEL_RANGE[1])) {
			errors.reject("article.level.error", new Object[] { LEVEL_RANGE[0], LEVEL_RANGE[1] },
					"文章级别范围应该在" + LEVEL_RANGE[0] + "和" + LEVEL_RANGE[1] + "之间");
			return;
		}
		String summary = article.getSummary();
		if (summary == null) {
			errors.reject("article.summary.blank", "文章摘要不能为空");
			return;
		}
		if (summary.length() > MAX_SUMMARY_LENGTH) {
			errors.reject("article.summary.toolong", new Object[] { MAX_SUMMARY_LENGTH },
					"文章摘要不能超过" + MAX_SUMMARY_LENGTH + "个字符");
			return;
		}
		String content = article.getContent();
		if (Validators.isEmptyOrNull(content, true)) {
			errors.reject("article.content.blank", "文章内容不能为空");
			return;
		}
		if (content.length() > MAX_CONTENT_LENGTH) {
			errors.reject("article.content.toolong", new Object[] { MAX_CONTENT_LENGTH },
					"文章内容不能超过" + MAX_CONTENT_LENGTH + "个字符");
			return;
		}
		String alias = article.getAlias();
		if (alias != null) {
			alias = alias.strip().toLowerCase();
			if (alias.isEmpty()) {
				article.setAlias(null);
			} else {
				try {
					Integer.parseInt(alias);
					errors.reject("article.alias.integer", "文章别名不能为数字");
					return;
				} catch (NumberFormatException e) {
				}
				if (alias.length() > MAX_ALIAS_LENGTH) {
					errors.reject("article.alias.toolong", new Object[] { MAX_ALIAS_LENGTH },
							"文章别名不能超过" + MAX_ALIAS_LENGTH + "个字符");
					return;
				}
				try {
					if (!alias.equals(URLEncoder.encode(alias, Constants.CHARSET.name()))) {
						errors.reject("article.alias.invalid", "文章别名校验失败");
						return;
					}
				} catch (UnsupportedEncodingException e) {
					throw new SystemException(e.getMessage(), e);
				}
				char[] chars = alias.toCharArray();
				for (char ch : chars) {
					if (ch == '/' || ch == '.') {
						errors.reject("article.alias.invalidChar", "文章别名校验不能包含'/'和'.'这些字符");
						return;
					}
				}
				article.setAlias(alias);
			}
		}

		String featureImage = article.getFeatureImage();
		if (!Validators.isEmptyOrNull(featureImage, true) && featureImage.length() > MAX_FEATURE_IMAGE_SIZE) {
			errors.reject("article.featureImage.toolong", new Object[] { MAX_FEATURE_IMAGE_SIZE },
					"文章特征图像不能超过" + MAX_FEATURE_IMAGE_SIZE + "个字符");
		}
	}
}
