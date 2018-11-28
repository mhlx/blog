package me.qyh.blog.core.plugin;

import me.qyh.blog.template.TemplateInterceptor;

public interface TemplateInterceptorRegistry {

	TemplateInterceptorRegistry register(TemplateInterceptor interceptor);

}
