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
package me.qyh.blog.template.render;

import me.qyh.blog.core.util.ExceptionUtils;

public class TemplateRenderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String templateName;
	private final TemplateRenderErrorDescription renderErrorDescription;
	private final boolean fromPreview;

	public TemplateRenderException(String templateName, TemplateRenderErrorDescription description, Throwable ex,
			boolean fromPreview) {
		super(null, ex, true, false);
		this.renderErrorDescription = description;
		this.templateName = templateName;
		this.fromPreview = fromPreview;
	}

	public TemplateRenderErrorDescription getRenderErrorDescription() {
		return renderErrorDescription;
	}

	/**
	 * root template name
	 * 
	 * @return
	 */
	public String getTemplateName() {
		return templateName;
	}

	public boolean isFromPreview() {
		return fromPreview;
	}

	/**
	 * 将错误栈写入 {@code TemplateRenderErrorDescription}
	 * 
	 * @see TemplateRenderErrorDescription#getStackTrace()
	 */
	public void writeStackTrace() {
		renderErrorDescription.setStackTrace(ExceptionUtils.getStackTrace(this));
	}

}