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

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.thymeleaf.spring5.naming.SpringContextVariableNames;

import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.web.Webs;

@Component
public class PageValidator implements Validator {

	public static final int PAGE_TPL_MAX_LENGTH = 500000;

	protected static final int PAGE_NAME_MAX_LENGTH = 20;
	protected static final int PAGE_DESCRIPTION_MAX_LENGTH = 500;
	protected static final int PAGE_ALIAS_MAX_LENGTH = 255;

	/**
	 * 最长深度 String.split("/").length
	 */
	public static final int MAX_ALIAS_DEPTH = 10;

	private static final String NO_REGISTRABLE_ALIAS_PATTERN = "^[A-Za-z0-9_-]+$";

	private static final String[] ALIAS_KEY_WORDS = { SpringContextVariableNames.SPRING_REQUEST_CONTEXT,
			AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE };

	@Override
	public boolean supports(Class<?> clazz) {
		return Page.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Page page = (Page) target;
		String pageTpl = page.getTpl();
		if (Validators.isEmptyOrNull(pageTpl, true)) {
			errors.reject("page.tpl.null", "页面模板不能为空");
			return;
		}

		if (pageTpl.length() > PAGE_TPL_MAX_LENGTH) {
			errors.reject("page.tpl.toolong", new Object[] { PAGE_TPL_MAX_LENGTH },
					"页面模板不能超过" + PAGE_TPL_MAX_LENGTH + "个字符");
			return;
		}

		String name = page.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("page.name.blank", "页面名称不能为空");
			return;
		}
		if (name.length() > PAGE_NAME_MAX_LENGTH) {
			errors.reject("page.name.toolong", new Object[] { PAGE_NAME_MAX_LENGTH },
					"页面名称不能超过" + PAGE_NAME_MAX_LENGTH + "个字符");
			return;
		}
		String description = page.getDescription();
		if (description == null) {
			errors.reject("page.description.null", "页面描述不能为空");
			return;
		}
		if (description.length() > PAGE_DESCRIPTION_MAX_LENGTH) {
			errors.reject("page.description.toolong", new Object[] { PAGE_DESCRIPTION_MAX_LENGTH },
					"页面描述不能超过" + PAGE_DESCRIPTION_MAX_LENGTH + "个字符");
			return;
		}

		if (page.isSpaceGlobal() && page.getSpace() != null) {
			errors.reject("page.spaceGlobal.hasSpace", "作用于全部空间的页面不能设置单独的空间");
			return;
		}

		String alias = validateAlias(page.getAlias(), errors);
		if (errors.hasErrors()) {
			return;
		}

		page.setAlias(alias);

		if (page.getAllowComment() == null) {
			errors.reject("page.allowComment", "是否允许评论不能为空");
		}
	}

	/**
	 * valid alias
	 * 
	 * @param alias
	 * @param errors
	 * @return
	 */
	public static String validateAlias(String alias, Errors errors) {
		if (Validators.isEmptyOrNull(alias, true)) {
			return "";
		}

		String cleanedAlias = FileUtils.cleanPath(alias).trim();

		if (cleanedAlias.isEmpty()) {
			return "";
		}

		if (cleanedAlias.length() > PAGE_ALIAS_MAX_LENGTH) {
			errors.reject("page.alias.toolong", new Object[] { PAGE_ALIAS_MAX_LENGTH },
					"页面别名不能超过" + PAGE_ALIAS_MAX_LENGTH + "个字符");
			return null;
		}
		validatePageAlias(cleanedAlias, errors);
		if (errors.hasErrors()) {
			return null;
		}
		return cleanedAlias;
	}

	private static void validatePageAlias(String alias, Errors errors) {

		if (Webs.getSpaceFromPath(alias, 1) != null) {
			errors.reject("page.alias.containsSpace", "路径中不能包含space信息");
			return;
		}

		if (alias.indexOf('/') == -1) {
			doValidatePageAlias(alias, true, errors);
		} else {

			String[] aliasArray = alias.split("/");
			if (aliasArray.length > MAX_ALIAS_DEPTH) {
				errors.reject("page.alias.depth.overmax", new Object[] { MAX_ALIAS_DEPTH },
						"路径最大深度不能超过" + MAX_ALIAS_DEPTH);
				return;
			}
			for (int i = 0; i < aliasArray.length; i++) {
				doValidatePageAlias(aliasArray[i], (i == aliasArray.length - 1), errors);
				if (errors.hasErrors()) {
					return;
				}
			}

			// 如果为PathVariable路径，判断variable是否相同
			String[] variableArray = StringUtils.substringsBetween(alias, "{", "}");

			if (variableArray != null && variableArray.length > 1
					&& Arrays.stream(variableArray).allMatch(s -> s.equals(variableArray[0]))) {
				errors.reject("page.alias.variable.same", "多个{}中间的内容不能相同");
			}

		}
	}

	private static void doValidatePageAlias(String alias, boolean last, Errors errors) {
		if (!last || alias.indexOf('.') == -1) {
			doValidatePageAlias(alias, errors);
		} else {
			// .test
			// test.
			if (alias.startsWith(".") || alias.endsWith(".")) {
				errors.reject("page.alias.last.startOrEndWithPoint", "最后一个路径不能以.开头或结尾");
				return;
			}
			// test..test
			if (org.springframework.util.StringUtils.countOccurrencesOf(alias, ".") > 1) {
				// 只能有一个 .
				errors.reject("page.alias.tooPoint", "路径只能有一个.");
				return;
			}
			String name = FileUtils.getNameWithoutExtension(alias);
			doValidatePageAlias(name, errors);

			if (errors.hasErrors()) {
				return;
			}

			String ext = FileUtils.getFileExtension(alias);
			if (!Validators.isAlpha(ext)) {
				errors.reject("page.alias.extension.invalid", "路径后缀必须是英文字母");
				return;
			}
		}
	}

	private static void doValidatePageAlias(String alias, Errors errors) {
		if (alias.startsWith("{")) {
			if (!alias.endsWith("}")) {
				errors.reject("page.alias.pathVariable.invalid", "以{开头必须以}结尾");
				return;
			}
			String variable = alias.substring(1, alias.length() - 1);
			if (!Validators.isLetter(variable)) {
				errors.reject("page.alias.pathVariable.onlyLetter", "{}中间的内容必须为英文字母");
				return;
			}

			// variable 不能为key word
			for (String keyword : ALIAS_KEY_WORDS) {
				if (variable.equals(keyword)) {
					errors.reject("page.alias.variable.keyword", new Object[] { keyword }, "{}中间的内容不能为" + keyword);
					return;
				}
			}
		} else {
			if (!alias.matches(NO_REGISTRABLE_ALIAS_PATTERN)) {
				errors.reject("page.alias.invalid", "路径只能为英文字母，数字或者-和_");
			}
		}
	}
}
