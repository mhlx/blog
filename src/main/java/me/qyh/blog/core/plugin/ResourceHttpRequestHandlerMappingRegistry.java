package me.qyh.blog.core.plugin;

import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public interface ResourceHttpRequestHandlerMappingRegistry {
	ResourceHttpRequestHandlerMappingRegistry registry(String urlPath, ResourceHttpRequestHandler handler);
}
