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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.WebExpressionContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.context.webmvc.SpringWebMvcThymeleafRequestContext;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;
import org.thymeleaf.spring5.naming.SpringContextVariableNames;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.render.ParseContextHolder;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRenderExecutor;
import me.qyh.blog.template.validator.FragmentValidator;

/**
 * 用来将模板解析成字符串
 * 
 * @author Administrator
 *
 */
public final class ThymeleafRenderExecutor implements TemplateRenderExecutor {

	@Autowired
	private ServletContext servletContext;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private TemplateEngine viewTemplateEngine;

	private static final String X_PJAX_Container_HEADER_NAME = "X-PJAX-Container";
	private static final String X_PJAX_FRAGMENT = "X-Fragment";
	private static final String X_FULLAPGE = "X-Fullpage";

	@Override
	public String execute(String viewTemplateName, final Map<String, Object> model, final HttpServletRequest request,
			final ReadOnlyResponse response) {
		return doExecutor(viewTemplateName, model, request, response);
	}

	@Override
	public String processPjaxTemplateName(String templateName, HttpServletRequest request) throws LogicException {
		Boolean fullpage = Boolean.parseBoolean(request.getHeader(X_FULLAPGE));
		if (fullpage) {
			return templateName;
		}
		String fragment = request.getHeader(X_PJAX_FRAGMENT);
		if (!Validators.isEmptyOrNull(fragment, true)) {
			fragment = FragmentValidator.validName(fragment, true);
			return Fragment.getTemplateName(fragment, Environment.getSpace());
		}
		String container = request.getHeader(X_PJAX_Container_HEADER_NAME);
		if (Validators.isEmptyOrNull(container, true)) {
			return templateName;
		} else {
			return templateName + " :: " + container;
		}
	}

	// COPIED FROM ThymeleafView 3.0.9.RELEASE
	private String doExecutor(String viewTemplateName, final Map<String, Object> model,
			final HttpServletRequest request, final HttpServletResponse response) {

		Objects.requireNonNull(viewTemplateName);

		Locale locale = LocaleContextHolder.getLocale();

		final Map<String, Object> mergedModel = new HashMap<>(30);

		// View.PATH_VARIABLES 只能获取被PathVariable annotation属性标记的属性
		// 这里需要获取optional PathVariable
		@SuppressWarnings("unchecked")
		final Map<String, Object> pathVars = (Map<String, Object>) request
				.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		if (pathVars != null) {
			mergedModel.putAll(pathVars);
		}
		if (model != null) {
			mergedModel.putAll(model);
		}

		final RequestContext requestContext = new RequestContext(request, response, servletContext, mergedModel);
		final SpringWebMvcThymeleafRequestContext thymeleafRequestContext = new SpringWebMvcThymeleafRequestContext(
				requestContext, request);

		// For compatibility with ThymeleafView
		addRequestContextAsVariable(mergedModel, SpringContextVariableNames.SPRING_REQUEST_CONTEXT, requestContext);
		// For compatibility with AbstractTemplateView
		addRequestContextAsVariable(mergedModel, AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE,
				requestContext);
		// Add the Thymeleaf RequestContext wrapper that we will be using in
		// this dialect (the bare RequestContext
		// stays in the context to for compatibility with other dialects)
		mergedModel.put(SpringContextVariableNames.THYMELEAF_REQUEST_CONTEXT, thymeleafRequestContext);

		final ConversionService conversionService = (ConversionService) request
				.getAttribute(ConversionService.class.getName());
		final NoRestrictedEvaluationContext evaluationContext = new NoRestrictedEvaluationContext(applicationContext,
				conversionService);

		mergedModel.put(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
				evaluationContext);

		final IEngineConfiguration configuration = viewTemplateEngine.getConfiguration();
		final WebExpressionContext context = new WebExpressionContext(configuration, request, response, servletContext,
				locale, mergedModel);

		final String templateName;
		final Set<String> markupSelectors;
		if (!viewTemplateName.contains("::")) {

			templateName = viewTemplateName;
			markupSelectors = null;

		} else {

			final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

			final FragmentExpression fragmentExpression;
			try {
				fragmentExpression = (FragmentExpression) parser.parseExpression(context,
						"~{" + viewTemplateName + "}");
			} catch (final TemplateProcessingException e) {
				throw new IllegalArgumentException("Invalid template name specification: '" + viewTemplateName + "'");
			}

			final FragmentExpression.ExecutedFragmentExpression fragment = FragmentExpression
					.createExecutedFragmentExpression(context, fragmentExpression);

			templateName = FragmentExpression.resolveTemplateName(fragment);
			markupSelectors = FragmentExpression.resolveFragments(fragment);
			final Map<String, Object> nameFragmentParameters = fragment.getFragmentParameters();

			if (nameFragmentParameters != null) {

				if (fragment.hasSyntheticParameters()) {
					// We cannot allow synthetic parameters because there is no way to specify them
					// at the template
					// engine execution!
					throw new IllegalArgumentException(
							"Parameters in a view specification must be named (non-synthetic): '" + viewTemplateName
									+ "'");
				}

				context.setVariables(nameFragmentParameters);

			}

		}

		final Set<String> processMarkupSelectors;
		if (markupSelectors != null && markupSelectors.size() > 0) {
			processMarkupSelectors = markupSelectors;
		} else {
			processMarkupSelectors = null;
		}
		String contentType = ParseContextHolder.getContext().getConfig().getContentType();
		TemplateSpec sec = new TemplateSpec(templateName, processMarkupSelectors, parseMode(contentType), null);
		return viewTemplateEngine.process(sec, context);
	}

	/**
	 * 根据模板名称来获取解析模式
	 * 
	 * @since 6.5
	 * @param templateName
	 * @return
	 */
	protected TemplateMode parseMode(String contentType) {
		if (Validators.isEmptyOrNull(contentType, true)) {
			return TemplateMode.HTML;
		}
		if (contentType.startsWith(MediaType.TEXT_HTML_VALUE)) {
			return TemplateMode.HTML;
		}
		if (contentType.startsWith(MediaType.APPLICATION_XML_VALUE)) {
			return TemplateMode.XML;
		}
		if (contentType.startsWith("text/css")) {
			return TemplateMode.CSS;
		}
		if (contentType.startsWith("text/javascript")) {
			return TemplateMode.JAVASCRIPT;
		}
		if (contentType.startsWith(MediaType.TEXT_PLAIN_VALUE)) {
			return TemplateMode.TEXT;
		}
		return TemplateMode.HTML;
	}

	private void addRequestContextAsVariable(final Map<String, Object> model, final String variableName,
			final RequestContext requestContext) throws TemplateProcessingException {

		if (model.containsKey(variableName)) {
			throw new TemplateProcessingException("属性" + variableName + "已经存在与request中");
		}
		model.put(variableName, requestContext);
	}
}
