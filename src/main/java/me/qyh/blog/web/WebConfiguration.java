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

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.ICacheManager;

import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.file.store.local.StaticResourceUrlHandlerMapping;
import me.qyh.blog.template.TemplateRequestMappingHandlerMapping;
import me.qyh.blog.template.render.TemplateRender;
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
	protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		returnValueHandlers.add(new TemplateReturnValueHandler(templateRender));
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
	public TemplateEngine templateEngine() {
		ThymeleafTemplateEngine templateEngine = new ThymeleafTemplateEngine();
		templateEngine.setEnableSpringELCompiler(true);
		templateEngine.setCacheManager(templateCacheManager());
		templateEngine.setTemplateResolvers(Set.of(thymeleafTemplateResolver()));
		return templateEngine;
	}

}