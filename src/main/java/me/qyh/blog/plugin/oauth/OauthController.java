package me.qyh.blog.plugin.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.LoginAuthenticator;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.util.StringUtils;

@Controller
@RequestMapping("oauth/{name}")
public class OauthController {

	@Autowired
	private LoginAuthenticator authenticator;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private UserService userService;

	private static final int COOKIE_SEC = 300;

	@GetMapping
	public String toAuthorizeUrl(@PathVariable("name") String name, HttpServletRequest request,
			HttpServletResponse resp) throws LogicException {
		if (Environment.hasAuthencated()) {
			return "redirect:" + urlHelper.getUrl();
		}

		OauthProvider provider = getProvider(name);

		String state = StringUtils.uuid();
		urlHelper.getCookieHelper().setCookie("state", state, COOKIE_SEC, true, request, resp);

		return "redirect:" + provider.getAuthorizeUrl(urlHelper.getUrls().getUrl("oauth/" + name), state);
	}

	@GetMapping(params = { "code", "state" })
	public String auth(@PathVariable("name") String name, @RequestParam("code") String code,
			@RequestParam("state") String state, @CookieValue("state") String cookieState, HttpServletRequest req,
			HttpServletResponse resp) throws LogicException {
		if (Environment.hasAuthencated()) {
			return "redirect:" + urlHelper.getUrl();
		}

		urlHelper.getCookieHelper().deleteCookie("state", req, resp);

		if (!cookieState.equals(state)) {
			throw new LogicException("oauth.state.invalid", "无效的状态码");
		}

		getProvider(name).validate(code, state, urlHelper.getUrls().getUrl("oauth/" + name));

		User user = userService.getUser();
		user.setPassword(null);

		if (authenticator.enable()) {
			req.getSession().setAttribute(Constants.GA_SESSION_KEY, user);
		} else {
			req.getSession().setAttribute(Constants.USER_SESSION_KEY, user);
			req.changeSessionId();
		}

		return "redirect:" + getRedirectUrl();
	}

	private String getRedirectUrl() {
		if (authenticator.enable()) {
			return urlHelper.getUrls().getUrl("login");
		} else {
			return urlHelper.getUrl();
		}
	}

	private OauthProvider getProvider(String name) throws LogicException {
		return OauthProviders.getProviders().getOauthProvider(name)
				.orElseThrow(() -> new LogicException("oauth.provider.invalid", "无效的oauth provider"));
	}

}
