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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.Builder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.plugin.HandlerInterceptorRegistry;
import me.qyh.blog.core.plugin.RequestMappingRegistry;
import me.qyh.blog.core.plugin.TemplateInterceptorRegistry;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.TemplateMapping.TemplateMatch;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.interceptor.AppInterceptor;
import me.qyh.blog.web.security.IPGetter;
import me.qyh.blog.web.view.TemplateView;

public class TemplateRequestMappingHandlerMapping extends RequestMappingHandlerMapping
		implements RequestMappingRegistry, TemplateInterceptorRegistry, HandlerInterceptorRegistry {

	private static final Method method;

	private final List<TemplateInterceptor> templateInterceptors = new ArrayList<>();

	static {
		try {
			method = TemplateHandler.class.getMethod("handlerTemplate");
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Autowired
	private TemplateMapping templateMapping;
	@Autowired(required = false)
	private IPGetter ipGetter;
	@Autowired
	private TemplateService templateService;

	private RequestMappingInfo.BuilderConfiguration config;

	private List<HandlerInterceptor> interceptors = new ArrayList<>();

	private HandlerInterceptor[] interceptorArray;

	/**
	 * 在应用启动期间，允许插件覆盖系统默认的路径
	 * 
	 * @since 6.5
	 */
	private HandlerMethodRegister register = (o, h, m) -> {
		synchronized (this) {
			super.unregisterMapping(m);
			super.registerHandlerMethod(o, h, m);
		}
	};

	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);

		String ip = ipGetter.getIp(request);
		request.setAttribute(Webs.IP_ATTR_NAME, ip);
		request.setAttribute(Webs.PREVIEW_ATTR_NAME, templateService.isPreviewIp(ip));

		if (Webs.errorRequest(request) || "GET".equals(request.getMethod())) {
			Optional<TemplateMatch> matchOptional;

			if (Webs.isPreview(request)) {
				matchOptional = templateMapping.getPreviewTemplateMapping()
						.getBestHighestPriorityTemplateMatch(lookupPath);
			} else {
				matchOptional = templateMapping.getBestHighestPriorityTemplateMatch(lookupPath);
			}

			if (matchOptional.isPresent()) {
				TemplateMatch match = matchOptional.get();
				setUriTemplateVariables(match, lookupPath, request);
				return new HandlerMethod(new TemplateHandler(match), method);
			}

		}

		return super.getHandlerInternal(request);
	}

	@Override
	protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> infos, String lookupPath, HttpServletRequest request)
			throws ServletException {
		if ("GET".equals(request.getMethod())) {
			Optional<TemplateMatch> matchOptional;

			if (Webs.isPreview(request)) {
				matchOptional = templateMapping.getPreviewTemplateMapping()
						.getBestPathVariableTemplateMatch(lookupPath);
			} else {
				matchOptional = templateMapping.getBestPathVariableTemplateMatch(lookupPath);
			}

			if (matchOptional.isPresent()) {
				TemplateMatch match = matchOptional.get();
				setUriTemplateVariables(match, lookupPath, request);
				return new HandlerMethod(new TemplateHandler(match), method);
			}
		}

		return super.handleNoMatch(infos, lookupPath, request);
	}

	@Override
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler
				: new HandlerExecutionChain(handler));
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		if (!templateInterceptors.isEmpty()) {
			getTemplateHandler(handler).ifPresent(th -> {
				String templateName = th.match.getTemplateName();
				for (TemplateInterceptor templateInterceptor : templateInterceptors) {
					if (templateInterceptor.match(templateName, request)) {
						chain.addInterceptor(templateInterceptor);
					}
				}
			});
		}
		addInterceptor(chain, getAdaptedInterceptors(), lookupPath);
		addInterceptor(chain, interceptorArray, lookupPath);
		return chain;
	}

	/**
	 * 注册一个新的Mapping
	 * 
	 * @param builder
	 *            RequestMappingInfo.Builder
	 * @param handler
	 * @param method
	 */
	public void registerMapping(RequestMappingInfo.Builder builder, Object handler, Method method) {
		super.registerMapping(builder.options(config).build(), handler, method);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		try {
			Field configField = this.getClass().getSuperclass().getDeclaredField("config");
			configField.setAccessible(true);
			this.config = (BuilderConfiguration) configField.get(this);
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		}

		if (ipGetter == null) {
			ipGetter = new IPGetter();
		}
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		this.register.register(handler, method, mapping);
	}

	@EventListener
	void start(ContextRefreshedEvent evt) {
		if (evt.getApplicationContext().getParent() == null) {
			return;
		}
		this.register = (o, h, m) -> super.registerHandlerMethod(o, h, m);
		templateInterceptors.addAll(BeanFactoryUtils
				.beansOfTypeIncludingAncestors(getApplicationContext(), TemplateInterceptor.class, true, false)
				.values());
	}

	private final class TemplateHandler {
		private final TemplateMatch match;

		public TemplateHandler(TemplateMatch match) {
			super();
			this.match = match;
		}

		@SuppressWarnings("unused")
		public TemplateView handlerTemplate() {
			return new TemplateView(match.getTemplateName(), match.getPattern());
		}
	}

	private Optional<TemplateHandler> getTemplateHandler(Object handler) {
		if (handler instanceof HandlerMethod) {
			Object bean = ((HandlerMethod) handler).getBean();
			return bean instanceof TemplateHandler ? Optional.of((TemplateHandler) bean) : Optional.empty();
		}
		return Optional.empty();
	}

	private void setUriTemplateVariables(TemplateMatch match, String lookupPath, HttpServletRequest request) {
		String pattern = match.getPattern();
		Map<String, String> pathVariables = getUrlPathHelper().decodePathVariables(request,
				getPathMatcher().extractUriTemplateVariables(pattern, FileUtils.cleanPath(lookupPath)));
		request.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);
	}

	@Override
	protected void extendInterceptors(List<Object> interceptors) {
		interceptors.add(getApplicationContext().getBean(AppInterceptor.class));
	}

	@Override
	public RequestMappingRegistry register(Builder builder, Object handler, Method method) {
		this.registerMapping(builder, handler, method);
		return this;
	}

	@Override
	public TemplateInterceptorRegistry register(TemplateInterceptor interceptor) {
		templateInterceptors.add(interceptor);
		return this;
	}

	@Override
	public HandlerInterceptorRegistry register(HandlerInterceptor handlerInterceptor) {
		interceptors.add(handlerInterceptor);
		this.interceptorArray = interceptors.toArray(HandlerInterceptor[]::new);
		return this;
	}

	private void addInterceptor(HandlerExecutionChain chain, HandlerInterceptor[] interceptors, String lookupPath) {
		if (Validators.isEmpty(interceptors)) {
			return;
		}
		for (HandlerInterceptor interceptor : interceptors) {
			if (interceptor instanceof MappedInterceptor) {
				MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
				if (mappedInterceptor.matches(lookupPath, getPathMatcher())) {
					chain.addInterceptor(mappedInterceptor.getInterceptor());
				}
			} else {
				chain.addInterceptor(interceptor);
			}
		}
	}

	private interface HandlerMethodRegister {
		void register(Object handler, Method method, RequestMappingInfo mapping);
	}

}
