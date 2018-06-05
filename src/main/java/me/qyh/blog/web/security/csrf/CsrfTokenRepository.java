package me.qyh.blog.web.security.csrf;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import me.qyh.blog.core.config.UrlHelper;

@Component
public final class CsrfTokenRepository {

	@Autowired
	private UrlHelper urlHelper;

	static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";

	public CsrfTokenRepository() {
		super();
	}

	public CsrfToken generateToken(HttpServletRequest request) {
		return new CsrfToken(createNewToken());
	}

	public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
		String tokenValue = token == null ? "" : token.getToken();
		urlHelper.getCookieHelper().setCookie(DEFAULT_CSRF_COOKIE_NAME, tokenValue, token == null ? 0 : -1, request,
				response);
	}

	public CsrfToken loadToken(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, DEFAULT_CSRF_COOKIE_NAME);
		if (cookie == null) {
			return null;
		}
		String token = cookie.getValue();
		if (!StringUtils.hasLength(token)) {
			return null;
		}
		return new CsrfToken(token);
	}

	public Optional<CsrfToken> changeToken(HttpServletRequest request, HttpServletResponse response) {
		boolean containsToken = loadToken(request) != null;
		if (containsToken) {
			saveToken(null, request, response);

			CsrfToken newToken = generateToken(request);
			saveToken(newToken, request, response);

			return Optional.of(newToken);
		}
		return Optional.empty();
	}

	protected String createNewToken() {
		return UUID.randomUUID().toString();
	}
}