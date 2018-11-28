package me.qyh.blog.web.security;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.util.Validators;

/**
 * 
 * 在代理环境下用来获取IP地址
 * <p>
 * <b>因为X-Forwarded-For可以被伪造，所以在没有代理的情况下不能用于直接获取IP地址</b>
 * </p>
 * 
 */
public class ProxyIPGetter extends IPGetter {

	@Override
	public String getIp(HttpServletRequest request) {
		String xForwardedForHeader = request.getHeader("X-Forwarded-For");
		if (!Validators.isEmptyOrNull(xForwardedForHeader, true)) {
			if (xForwardedForHeader.indexOf(',') == -1) {
				return xForwardedForHeader;
			} else {
				return xForwardedForHeader.split(",")[0];
			}
		}
		return super.getIp(request);
	}

}
