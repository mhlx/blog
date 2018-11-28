package me.qyh.blog.core.plugin;

import org.springframework.web.servlet.HandlerInterceptor;

public interface HandlerInterceptorRegistry {
	HandlerInterceptorRegistry register(HandlerInterceptor handlerInterceptor);
}
