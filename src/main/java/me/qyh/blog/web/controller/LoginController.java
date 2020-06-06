package me.qyh.blog.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.exception.LoginFailException;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.security.LoginChain;
import me.qyh.blog.security.TokenUtil;
import me.qyh.blog.service.BlogConfigService;

@RestController
public class LoginController {

	private final List<LoginChain> chains;
	private final BlogConfigService configService;
	private final CaptchaValidator captchaValidator;
	private final Bucket bucket;
	private final BlogProperties blogProperties;

	public LoginController(BlogConfigService configService, List<LoginChain> chains, BlogProperties blogProperties,
			CaptchaValidator captchaValidator) {
		super();
		this.configService = configService;
		this.chains = chains;
		this.chains.add(new DefaultLoginChain());
		this.bucket = blogProperties.getLoginBucket();
		this.captchaValidator = captchaValidator;
		this.blogProperties = blogProperties;
		OrderComparator.sort(this.chains);
	}

	@PostMapping("api/token")
	public String token(@RequestBody JsonNode node, HttpServletRequest request) {

		if (!bucket.tryConsume(1)) {
			JsonNode keyNode = node.get("captcha_key");
			JsonNode valueNode = node.get("captcha_value");
			captchaValidator.validate(keyNode == null ? null : keyNode.asText(),
					valueNode == null ? null : valueNode.asText());
		}

		for (LoginChain chain : chains) {
			chain.valid(node);
		}

		if (!blogProperties.isCors()) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(Constants.AUTHENTICATED_SESSION_KEY);
			}
		}

		if (!blogProperties.isCors()) {
			request.getSession().setAttribute(Constants.AUTHENTICATED_SESSION_KEY, Boolean.TRUE);
		}

		return TokenUtil.create();
	}

	private final class DefaultLoginChain implements LoginChain {

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void valid(JsonNode node) {
			JsonNode nameNode = node.get("username");
			JsonNode passwordNode = node.get("password");
			if (nameNode == null || passwordNode == null
					|| !configService.authenticate(nameNode.asText(), passwordNode.asText())) {
				throw new LoginFailException(new Message("login.fail", "登录失败"));
			}
		}
	}
}
