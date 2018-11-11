/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.qyh.blog.core.util;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;

/**
 * Provides static methods for composing URLs.
 * <p>
 * Placed into a separate class for visibility, so that changes to URL
 * formatting conventions will affect all users.
 *
 * @author Ben Alex
 */
public final class UrlUtils {

	private static final AntPathMatcher APM = new AntPathMatcher();

	/**
	 * private
	 */
	private UrlUtils() {
		super();
	}
	// ~ Methods
	// ========================================================================================================

	/**
	 * 获取请求的完整链接
	 * 
	 * @param r
	 *            当前请求
	 * @return 完整的链接
	 */
	public static String buildFullRequestUrl(HttpServletRequest r) {
		return buildFullRequestUrl(r.getScheme(), r.getServerName(), r.getServerPort(), r.getRequestURI(),
				r.getQueryString());
	}

	/**
	 * Obtains the full URL the client used to make the request.
	 * <p>
	 * Note that the server port will not be shown if it is the default server port
	 * for HTTP or HTTPS (80 and 443 respectively).
	 *
	 * @return the full URL, suitable for redirects (not decoded).
	 */
	public static String buildFullRequestUrl(String _scheme, String serverName, int serverPort, String requestURI,
			String queryString) {

		String scheme = _scheme.toLowerCase();

		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(serverName);

		// Only add port if not default
		if ("http".equals(scheme)) {
			if (serverPort != 80) {
				url.append(":").append(serverPort);
			}
		} else if ("https".equals(scheme) && (serverPort != 443)) {
			url.append(":").append(serverPort);
		}

		// Use the requestURI as it is encoded (RFC 3986) and hence suitable for
		// redirects.
		url.append(requestURI);

		if (queryString != null) {
			url.append("?").append(queryString);
		}

		return url.toString();
	}

	/**
	 * Decides if a URL is absolute based on whether it contains a valid scheme
	 * name, as defined in RFC 1738.
	 */
	public static boolean isAbsoluteUrl(String url) {
		final Pattern ABSOLUTE_URL = Pattern.compile("\\A[a-z0-9.+-]+://.*", Pattern.CASE_INSENSITIVE);

		return ABSOLUTE_URL.matcher(url).matches();
	}

	/**
	 * 
	 * @see AntPathMatcher
	 * @param pattern
	 * @param path
	 * @return
	 */
	public static boolean match(String pattern, String path) {
		return APM.match(pattern, path);
	}

	/**
	 * @since 7.0
	 * @param request
	 * @return
	 */
	public static String getRequestURIWithoutContextPath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getContextPath().length() + 1);
	}
}