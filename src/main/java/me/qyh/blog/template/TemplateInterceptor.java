package me.qyh.blog.template;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * <p>
 * 模板拦截器，等效于HandlerInterceptor
 * </p>
 * <p>
 * <b>无法拦截Fragment、非注册PathTemplate，因为它本质上还是基于路径的拦截器</b>
 * </p>
 * 
 * @author mhlx
 * 
 * @since 5.6
 *
 */
public interface TemplateInterceptor extends AsyncHandlerInterceptor {

	boolean match(String templateName, HttpServletRequest request);

}
