package me.qyh.blog.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;

public final class BlogContextFilter implements Filter {

	private final BlogProperties blogProperties;

	public BlogContextFilter(BlogProperties blogProperties) {
		super();
		this.blogProperties = blogProperties;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			setContext((HttpServletRequest) request);
			chain.doFilter(request, response);
		} finally {
			BlogContext.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private void setContext(HttpServletRequest request) {
		String ipHeader = blogProperties.getIpHeader();
		String ip;
		if (ipHeader != null) {
			ip = request.getHeader(ipHeader);
		} else {
			ip = request.getRemoteAddr();
		}
		BlogContext.setIP(ip);
		HttpSession session = request.getSession(false);
		if (session != null) {
			if (session.getAttribute(Constants.AUTH_SESSION_KEY) != null) {
				BlogContext.setAuthenticated(true);
			}
			BlogContext.setPasswordMap((Map<String, String>) session.getAttribute(Constants.PASSWORD_SESSION_KEY));
		}
	}
}