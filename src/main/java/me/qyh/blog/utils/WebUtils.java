package me.qyh.blog.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;

public class WebUtils {

    private static final AntPathMatcher apm = new AntPathMatcher();

    private static final String[] SPIDERS = new String[]{"Googlebot", "Baiduspider", "360Spider", "Bingbot", "msnbot",
            "DuckDuckBot", "slurp", "YandexBot", "Sogou", "YoudaoBot", "Sosospider", "Yisouspider"};

    private WebUtils() {
        super();
    }

    public static boolean isSpider(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isNullOrBlank(userAgent)) {
            return false;
        }
        return Arrays.stream(SPIDERS).anyMatch(userAgent::contains);
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        return request instanceof MultipartHttpServletRequest;// ??
    }

    public static boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length() + 1);
        if (path.startsWith("api/")) {
            return true;
        }
        String forward = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        return forward != null && forward.startsWith("/api/");
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
