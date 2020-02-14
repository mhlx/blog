package me.qyh.blog.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.web.AjaxError;

/**
 * 
 * @author Administrator
 */
@Controller
@RequestMapping
@ConditionalOnProperty(prefix = "blog.core", name = "totp-enable", havingValue = "true")
public class TOTPValidatorController {

	private final TOTPAuthenticator authenticator;
	private final Bucket bucket;
	private final CaptchaValidator captchaValidator;

	public TOTPValidatorController(TOTPAuthenticator authenticator, BlogProperties blogProperties,
			CaptchaValidator captchaValidator) {
		super();
		this.authenticator = authenticator;
		this.bucket = blogProperties.getLoginBucket();
		this.captchaValidator = captchaValidator;
	}

	@ResponseBody
	@PostMapping(value = "session", params = { "totpCode" })
	public ResponseEntity<Object> login(@RequestParam("totpCode") String code, HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Constants.AUTH_TOTP_SESSION) == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AjaxError(new Message("user.authRequired", "请先验证用户名密码")));
		}
		if (!bucket.tryConsume(1)) {
			captchaValidator.validate(request);
		}
		if (!authenticator.check(code)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AjaxError(new Message("otp.authFail", "二次认证失败")));
		}
		session.setAttribute(Constants.AUTH_SESSION_KEY, session.getAttribute(Constants.AUTH_TOTP_SESSION));
		session.removeAttribute(Constants.AUTH_TOTP_SESSION);
		return ResponseEntity.noContent().build();
	}
}
