package me.qyh.blog.web.security;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * 用来获取客户端请求的IP地址
 * 
 */
public class IPGetter {

	public String getIp(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

}
