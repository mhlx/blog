/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
