package me.qyh.blog.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.AbstractFlashMapManager;
import org.springframework.web.util.WebUtils;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.util.StringUtils;

public class CookieMemoryLinkFlashMapManager extends AbstractFlashMapManager {

	@Autowired
	private UrlHelper urlHelper;

	private static final String MESSAGE_ID = "messageId";
	private final Map<String, List<FlashMap>> map = new ConcurrentHashMap<>();

	@Override
	protected List<FlashMap> retrieveFlashMaps(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, MESSAGE_ID);
		if (cookie == null) {
			return null;
		}
		return map.get(cookie.getValue());
	}

	@Override
	protected void updateFlashMaps(List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = WebUtils.getCookie(request, MESSAGE_ID);
		if (CollectionUtils.isEmpty(flashMaps)) {
			if (cookie != null) {
				map.remove(cookie.getValue());
				// setCookie(cookie, "", 0, response);
			}
		} else {
			String id = StringUtils.uuid();
			map.put(id, flashMaps);
			urlHelper.getCookieHelper().setCookie(MESSAGE_ID, id, -1, request, response);
		}
	}

}
