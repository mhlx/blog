package me.qyh.blog.file.store.local;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import me.qyh.blog.core.plugin.ResourceHttpRequestHandlerMappingRegistry;

/**
 * UrlMapping，用來注册静态文件处理器
 */
public class StaticResourceUrlHandlerMapping extends SimpleUrlHandlerMapping
		implements ResourceHttpRequestHandlerMappingRegistry {

	public void registerResourceHttpRequestHandlerMapping(String urlPath, ResourceHttpRequestHandler handler)
			throws BeansException, IllegalStateException {
		super.registerHandler(urlPath, handler);
	}

	@Override
	public ResourceHttpRequestHandlerMappingRegistry registry(String urlPath, ResourceHttpRequestHandler handler) {
		registerHandler(urlPath, handler);
		return this;
	}

}
