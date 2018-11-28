package me.qyh.blog.core.plugin;

import me.qyh.blog.web.ExceptionHandler;

public interface ExceptionHandlerRegistry {

	ExceptionHandlerRegistry register(ExceptionHandler exceptionHandler);

}
