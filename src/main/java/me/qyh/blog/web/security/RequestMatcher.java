package me.qyh.blog.web.security;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface RequestMatcher {

	/**
	 * 是否匹配请求
	 * 
	 * @param request
	 *            当前请求
	 * @return 是否匹配
	 */
	boolean match(HttpServletRequest request);

}
