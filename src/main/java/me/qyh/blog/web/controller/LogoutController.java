package me.qyh.blog.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.security.TokenUtil;

@Authenticated
@RestController
public class LogoutController {

	private final BlogProperties blogProperties;

	public LogoutController(BlogProperties blogProperties) {
		super();
		this.blogProperties = blogProperties;
	}

	@DeleteMapping("api/token")
	public void logout(HttpServletRequest request) {
		TokenUtil.remove();
		if (!blogProperties.isCors()) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(Constants.AUTHENTICATED_SESSION_KEY);
			}
		}
	}

}
