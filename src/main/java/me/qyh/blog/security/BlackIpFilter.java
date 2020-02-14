package me.qyh.blog.security;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import me.qyh.blog.BlogContext;

public class BlackIpFilter implements Filter {

	private final BlackIpService blackIpService;

	public BlackIpFilter(BlackIpService blackIpService) {
		super();
		this.blackIpService = blackIpService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Optional<String> opIp = BlogContext.getIP();
		if (opIp.isEmpty() || blackIpService.isBlackIp(opIp.get())) {
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		chain.doFilter(request, response);
	}

}
