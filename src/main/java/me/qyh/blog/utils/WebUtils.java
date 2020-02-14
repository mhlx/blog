package me.qyh.blog.utils;

import java.net.URI;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class WebUtils {

	public static final String AJAX_HEADER = "x-requested-with=XMLHttpRequest";
	private static final AntPathMatcher apm = new AntPathMatcher();

	private static final String[] SPIDERS = new String[] { "Googlebot", "Baiduspider", "360Spider", "Bingbot", "msnbot",
			"DuckDuckBot", "slurp", "YandexBot", "Sogou", "YoudaoBot", "Sosospider", "Yisouspider" };

	private WebUtils() {
		super();
	}

	public static boolean isSpider(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if (StringUtils.isNullOrBlank(userAgent)) {
			return false;
		}
		return Arrays.stream(SPIDERS).filter(userAgent::contains).findAny().isPresent();
	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			return true;
		}
		return request instanceof MultipartHttpServletRequest;// ??
	}

	public static boolean isConsoleRequest(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length() + 1);
		if (path.startsWith("console/")) {
			return true;
		}
		return false;
	}

	public static boolean isAbsoluteWebUrl(String url) {
		if (!org.springframework.util.StringUtils.startsWithIgnoreCase(url, "http://")
				&& !org.springframework.util.StringUtils.startsWithIgnoreCase(url, "https://")) {
			return false;
		}
		try {
			return URI.create(url).isAbsolute();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean isPattern(String path) {
		return apm.isPattern(path);
	}
}
