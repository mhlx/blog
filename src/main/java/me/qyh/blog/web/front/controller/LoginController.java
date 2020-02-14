package me.qyh.blog.web.front.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.service.BlogConfigService;
import me.qyh.blog.vo.User;
import me.qyh.blog.web.AjaxError;

@Controller
@RequestMapping
public class LoginController {

	private final BlogConfigService blogConfigService;
	private final boolean totpEnable;
	private final Bucket bucket;
	private final CaptchaValidator captchaValidator;

	public LoginController(BlogConfigService blogConfigService, BlogProperties blogProperties,
			CaptchaValidator captchaValidator) {
		super();
		this.blogConfigService = blogConfigService;
		this.totpEnable = blogProperties.isTotpEnable();
		this.bucket = blogProperties.getLoginBucket();
		this.captchaValidator = captchaValidator;
	}

	@GetMapping("login")
	public String index() {
		return "login";
	}

	@GetMapping("isAuthenticated")
	@ResponseBody
	public boolean isAuthenticated(HttpServletRequest request) {
		return BlogContext.isAuthenticated();
	}

	@ResponseBody
	@PostMapping(value = "session", params = { "name", "password" })
	public ResponseEntity<Object> login(@RequestParam("name") String name, @RequestParam("password") String password,
			HttpServletRequest request) {
		if (!bucket.tryConsume(1)) {
			captchaValidator.validate(request);
		}
		User user = blogConfigService.authenticate(name, password);
		HttpSession session = request.getSession();
		if (totpEnable) {
			session.setAttribute(Constants.AUTH_TOTP_SESSION, user);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AjaxError(new Message("totp.authRequired", "请进行登录二次验证")));
		} else {
			session.setAttribute(Constants.AUTH_SESSION_KEY, user);
		}
		return ResponseEntity.noContent().build();
	}

}
