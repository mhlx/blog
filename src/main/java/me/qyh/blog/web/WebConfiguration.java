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
package me.qyh.blog.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.file.store.local.StaticResourceUrlHandlerMapping;
import me.qyh.blog.template.TemplateRequestMappingHandlerMapping;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRender;
import me.qyh.blog.template.render.TemplateRenderException;
import me.qyh.blog.template.render.thymeleaf.ThymeleafCacheManager;
import me.qyh.blog.template.render.thymeleaf.ThymeleafTemplateEngine;
import me.qyh.blog.template.render.thymeleaf.ThymeleafTemplateResolver;
import me.qyh.blog.web.view.TemplateReturnValueHandler;

/**
 * 替代默认的RequestMappingHandlerMapping
 * 
 * @author Administrator
 *
 */
@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {

	@Autowired
	private WebExceptionResolver exceptionResolver;
	@Autowired
	private TemplateRender templateRender;

	private static final Integer cacheSec = 31556926;

	@Bean
	@Override
	public TemplateRequestMappingHandlerMapping requestMappingHandlerMapping() {
		return (TemplateRequestMappingHandlerMapping) super.requestMappingHandlerMapping();
	}

	@Bean
	@Override
	public StaticResourceUrlHandlerMapping resourceHandlerMapping() {
		SimpleUrlHandlerMapping mapping = (SimpleUrlHandlerMapping) super.resourceHandlerMapping();
		StaticResourceUrlHandlerMapping fsMapping = new StaticResourceUrlHandlerMapping();
		fsMapping.setOrder(-100);
		fsMapping.setUrlMap(mapping.getUrlMap());
		return fsMapping;
	}

	@Override
	protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
		return new TemplateRequestMappingHandlerMapping();
	}

	@Override
	protected void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/favicon.ico").setCachePeriod(cacheSec)
				.addResourceLocations("/static/img/favicon.ico");
		registry.addResourceHandler("/doc/**").setCachePeriod(cacheSec).addResourceLocations("/doc/");
		registry.addResourceHandler("/static/**").setCachePeriod(cacheSec).addResourceLocations("/static/");
	}

	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		addDefaultHttpMessageConverters(converters);
		HttpMessageConverter<?> toRemove = null;
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof GsonHttpMessageConverter) {
				toRemove = converter;
				break;
			}
			// @since 7.0
			if (converter instanceof StringHttpMessageConverter) {
				StringHttpMessageConverter shmc = (StringHttpMessageConverter) converter;
				shmc.setDefaultCharset(Constants.CHARSET);
			}
		}
		if (toRemove != null) {
			converters.remove(toRemove);
		}

		// 替代默认的GsonHttpMessageConverter
		GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
		msgConverter.setGson(Jsons.getGson());
		converters.add(msgConverter);
	}

	@Override
	protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
		return new WebRequestMappingHandlerAdapter();
	}

	@Override
	protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
		return new WebExceptionHandlerExceptionResolver();
	}

	@Override
	protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		resolvers.add(exceptionResolver);
	}

	@Bean
	public ICacheManager templateCacheManager() {
		return new ThymeleafCacheManager();
	}

	@Bean
	public ThymeleafTemplateResolver thymeleafTemplateResolver() {
		return new ThymeleafTemplateResolver();
	}

	@Bean
	public SpringResourceTemplateResolver springResourceTemplateResolver() {
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(getApplicationContext());
		templateResolver.setPrefix("/WEB-INF/templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding(Constants.CHARSET.name());
		return templateResolver;
	}

	@Bean
	public TemplateEngine templateEngine() {
		ThymeleafTemplateEngine templateEngine = new ThymeleafTemplateEngine();
		templateEngine.setEnableSpringELCompiler(true);
		templateEngine.setCacheManager(templateCacheManager());
		templateEngine.setTemplateResolvers(Set.of(thymeleafTemplateResolver(), springResourceTemplateResolver()));
		return templateEngine;
	}

	@Override
	protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		returnValueHandlers.add(new TemplateReturnValueHandler(templateRender));
	}

	private final class WebViewNameMethodReturnValueHandler extends ViewNameMethodReturnValueHandler {

		private final TemplateRender templateRender;

		public WebViewNameMethodReturnValueHandler(TemplateRender templateRender) {
			super();
			this.templateRender = templateRender;
		}

		@Override
		public void handleReturnValue(Object returnValue, MethodParameter returnType,
				ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
			if (returnValue instanceof CharSequence) {
				String viewName = returnValue.toString();
				mavContainer.setViewName(viewName);
				if (isRedirectViewName(viewName)) {
					mavContainer.setRedirectModelScenario(true);
				} else {
					mavContainer.setRequestHandled(true);

					HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);
					HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);

					String content;

					try {
						content = templateRender.doRender(viewName, mavContainer.getModel(), nativeRequest,
								new ReadOnlyResponse(nativeResponse), new ParseConfig());
					} catch (TemplateRenderException | SystemException e) {
						throw e;
					} catch (Exception e) {
						throw new SystemException(e.getMessage(), e);
					}

					nativeResponse.setContentType(MediaType.TEXT_HTML_VALUE);
					nativeResponse.setCharacterEncoding(Constants.CHARSET.name());
					nativeResponse.getWriter().write(content);
					nativeResponse.getWriter().flush();
				}
			} else if (returnValue != null) {
				// should not happen
				throw new UnsupportedOperationException("Unexpected return type: "
						+ returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
			}
		}
	}

	private final class WebRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

		@Override
		public void afterPropertiesSet() {
			super.afterPropertiesSet();
			List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(getReturnValueHandlers());
			handlers.removeIf(handler -> handler instanceof ViewNameMethodReturnValueHandler);
			handlers.add(new WebViewNameMethodReturnValueHandler(templateRender));
			super.setReturnValueHandlers(handlers);
		}
	}

	private final class WebExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver {
		@Override
		public void afterPropertiesSet() {
			super.afterPropertiesSet();
			HandlerMethodReturnValueHandlerComposite composite = super.getReturnValueHandlers();
			List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(composite.getHandlers());
			handlers.removeIf(handler -> handler instanceof ViewNameMethodReturnValueHandler);
			handlers.add(new WebViewNameMethodReturnValueHandler(templateRender));
			super.setReturnValueHandlers(handlers);
		}
	}

}