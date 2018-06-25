/*
 * Copyright 2018 qyh.me
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
package me.qyh.blog.template;

import java.util.Optional;

public final class PreviewTemplate implements Template {
	private final Template template;

	/**
	 * 预览模板前缀
	 */
	private static String TEMPLATE_PREVIEW_PREFIX = TEMPLATE_PREFIX + "Preview" + SPLITER;

	public Template getOriginalTemplate() {
		return template;
	}

	public PreviewTemplate(Template template) {
		super();
		this.template = template;
	}

	@Override
	public String getTemplate() {
		return template.getTemplate();
	}

	@Override
	public String getTemplateName() {
		return TEMPLATE_PREVIEW_PREFIX + template.getTemplateName();
	}

	@Override
	public Template cloneTemplate() {
		return new PreviewTemplate(template);
	}

	@Override
	public boolean isCallable() {
		return template.isCallable();
	}

	@Override
	public boolean equalsTo(Template other) {
		return false;
	}

//	@Override
//	public String getRelativePath() {
//		return template.getRelativePath();
//	}
//
//	@Override
//	public boolean hasPathVariable() {
//		return template.hasPathVariable();
//	}

	@Override
	public boolean cacheable() {
		return false;
	}

	/**
	 * 判断是否是预览模板文件名
	 * 
	 * @param templateName
	 * @return
	 */
	public static boolean isPreviewTemplate(String templateName) {
		return templateName != null && templateName.startsWith(TEMPLATE_PREVIEW_PREFIX);
	}

	public static String getTemplateName(String templateName) {
		if (isPreviewTemplate(templateName)) {
			return templateName;
		}
		return TEMPLATE_PREVIEW_PREFIX + templateName;
	}

	public static Optional<String> getOriginalTemplateName(String previewTemplateName) {
		if (isPreviewTemplate(previewTemplateName)) {
			return Optional.of(previewTemplateName.substring(TEMPLATE_PREVIEW_PREFIX.length()));
		}
		return Optional.empty();
	}
}