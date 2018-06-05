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
package me.qyh.blog.template.render.thymeleaf;

import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResource;
import org.thymeleaf.exceptions.TemplateProcessingException;

import me.qyh.blog.core.util.ExceptionUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.render.TemplateExceptionTranslater;
import me.qyh.blog.template.render.TemplateRenderErrorDescription;
import me.qyh.blog.template.render.TemplateRenderErrorDescription.TemplateErrorInfo;
import me.qyh.blog.template.render.TemplateRenderException;

public class ThymleafTemplateExceptionTransalater implements TemplateExceptionTranslater {

	private static final String SPEL_EXPRESSION_ERROR_PREFIX = "Exception evaluating SpringEL expression:";
	private static final String STANDARD_EXPRESSION_ERROR_PREFIX = "Could not parse as expression:";
	/**
	 * @see ServletContextResource#getDescription()
	 */
	private static final String SERVLET_RESOURCE_PREFIX = "ServletContext resource ";

	@Override
	public Optional<TemplateRenderException> translate(String templateName, Throwable e) {
		return translate(templateName, e, true, true);
	}

	@Override
	public Optional<TemplateRenderException> translateNoFillTrace(String templateName, Throwable e) {
		return translate(templateName, e, false, false);
	}

	private Optional<TemplateRenderException> translate(String templateName, Throwable e, boolean enableSuppression,
			boolean writableStackTrace) {
		if (e instanceof TemplateProcessingException) {
			return Optional.of(new TemplateRenderException(templateName,
					fromException((TemplateProcessingException) e, templateName, writableStackTrace), e,
					enableSuppression, writableStackTrace));
		}
		return Optional.empty();
	}

	private TemplateRenderErrorDescription fromException(TemplateProcessingException e, String templateName,
			boolean writableStackTrace) {
		TemplateRenderErrorDescription description = new TemplateRenderErrorDescription();
		List<Throwable> ths = ExceptionUtils.getThrowableList(e);
		TemplateProcessingException last = null;
		for (Throwable th : ths) {
			if (TemplateProcessingException.class.isAssignableFrom(th.getClass())) {
				TemplateProcessingException templateProcessingException = (TemplateProcessingException) th;
				String templateName2 = templateProcessingException.getTemplateName();
				if (!Validators.isEmptyOrNull(templateName2, true)) {
					templateName2 = parseTemplateName(templateName2);
					description.addTemplateErrorInfos(new TemplateErrorInfo(templateName2,
							templateProcessingException.getLine(), templateProcessingException.getCol()));
					last = templateProcessingException;
				}
			}
		}
		if (last != null) {
			last.setTemplateName(null);
			description.setExpression(tryGetExpression(last.getMessage()));
		}
		if (writableStackTrace) {
			description.setStackTrace(ExceptionUtils.getStackTrace(e));
		}
		return description;
	}

	private String tryGetExpression(String errorMsg) {
		if (errorMsg.startsWith(SPEL_EXPRESSION_ERROR_PREFIX)) {
			String expression = StringUtils.delete(errorMsg, SPEL_EXPRESSION_ERROR_PREFIX).trim();
			return expression.substring(1, expression.length() - 1);
		}
		if (errorMsg.startsWith(STANDARD_EXPRESSION_ERROR_PREFIX)) {
			String expression = StringUtils.delete(errorMsg, STANDARD_EXPRESSION_ERROR_PREFIX).trim();
			return expression.substring(1, expression.length() - 1);
		}
		return null;
	}

	private String parseTemplateName(String name) {
		if (name.startsWith(SERVLET_RESOURCE_PREFIX)) {
			int first = name.indexOf('[');
			int last = name.lastIndexOf(']');
			if (first != -1 && last != -1) {
				return name.substring(first + 1, last);
			}
		}
		return name;
	}

}
