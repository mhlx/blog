package me.qyh.blog.web.controller.front;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.AttemptLoggerManager;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.validator.LoginBeanValidator;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.LoginBean;
import me.qyh.blog.template.TemplateRequestMappingHandlerMapping;
import me.qyh.blog.web.RememberMeService;
import me.qyh.blog.web.security.CaptchaValidator;
import me.qyh.blog.web.security.csrf.CsrfTokenRepository;

@Controller("loginController")
public class LoginController implements InitializingBean {

	@Autowired
	private LoginBeanValidator loginBeanValidator;
	@Autowired
	private UserService userService;

	@Autowired(required = false)
	private GoogleAuthenticator ga;

	@Autowired
	private CaptchaValidator captchaValidator;

	@Autowired
	private TemplateRequestMappingHandlerMapping mapping;

	@Autowired(required = false)
	private CsrfTokenRepository csrfTokenRepository;

	private final Message otpVerifyFail = new Message("otp.verifyFail", "动态口令校验失败");
	private final Message pwdVerifyRequire = new Message("pwd.verifyRequire", "请先通过密码验证");

	@Value("${login.attempt.count:5}")
	private int attemptCount;

	@Value("${login.attempt.maxCount:100}")
	private int maxAttemptCount;

	@Value("${login.attempt.sleepSec:300}")
	private int sleepSec;

	@Autowired
	private AttemptLoggerManager attemptLoggerManager;
	private AttemptLogger attemptLogger;

	@Autowired
	private RememberMeService rememberMeService;

	private static final String REMEMBER_ME_GA = "rememberMeGa";

	@InitBinder(value = "loginBean")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(loginBeanValidator);
	}

	@PostMapping(value = "login")
	@ResponseBody
	public JsonResult login(@RequestBody @Validated LoginBean loginBean, HttpServletRequest request,
			HttpServletResponse response) throws LogicException {
		String ip = Environment.getIP();
		if (attemptLogger.log(ip)) {
			captchaValidator.doValidate(request);
		}
		User user = userService.login(loginBean);
		if (ga != null) {
			HttpSession session = request.getSession();
			if (loginBean.isRememberMe()) {
				session.setAttribute(REMEMBER_ME_GA, Boolean.TRUE);
			}
			session.setAttribute(Constants.GA_SESSION_KEY, user);
			return new JsonResult(false, new Message("otp.required", "请输入动态口令"));
		}
		successLogin(user, request, response);
		if (loginBean.isRememberMe()) {
			rememberMeService.rememberMe(user, request, response);
		}
		return new JsonResult(true);
	}

	@GetMapping("login/needCaptcha")
	@ResponseBody
	public boolean needCaptcha() {
		return attemptLogger.reach(Environment.getIP());
	}

	@ResponseBody
	public JsonResult otpVerify(@RequestParam("code") String codeStr, HttpServletRequest request,
			HttpServletResponse response) throws LogicException {
		enableOtpRequired();
		HttpSession session = request.getSession(false);
		if (session == null) {
			return new JsonResult(false, pwdVerifyRequire);
		}
		// 没有通过用户名密码认证，无需校验
		User user = (User) session.getAttribute(Constants.GA_SESSION_KEY);
		if (user == null) {
			return new JsonResult(false, pwdVerifyRequire);
		}

		String ip = Environment.getIP();
		if (attemptLogger.log(ip)) {
			captchaValidator.doValidate(request);
		}

		if (!ga.checkCode(codeStr)) {
			return new JsonResult(false, otpVerifyFail);
		}
		session.removeAttribute(Constants.GA_SESSION_KEY);
		successLogin(user, request, response);
		if (session.getAttribute(REMEMBER_ME_GA) != null) {
			session.removeAttribute(REMEMBER_ME_GA);
			rememberMeService.rememberMe(user, request, response);
		}
		return new JsonResult(true);
	}

	private void successLogin(User user, HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		session.setAttribute(Constants.USER_SESSION_KEY, user);
		changeSessionId(request);
		attemptLogger.remove(Environment.getIP());
		changeCsrf(request, response);
	}

	private void changeSessionId(HttpServletRequest request) {
		request.changeSessionId();
	}

	private void enableOtpRequired() throws LogicException {
		if (ga == null) {
			throw new LogicException("otp.required", "需要OTP支持");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.attemptLogger = attemptLoggerManager.createAttemptLogger(attemptCount, maxAttemptCount, sleepSec);

		if (ga != null) {

			mapping.registerMapping(RequestMappingInfo.paths("login/otpVerify").methods(RequestMethod.POST),
					"loginController", LoginController.class.getMethod("otpVerify", String.class,
							HttpServletRequest.class, HttpServletResponse.class));

		}
	}

	private void changeCsrf(HttpServletRequest request, HttpServletResponse response) {
		if (csrfTokenRepository == null) {
			return;
		}
		csrfTokenRepository.changeToken(request, response);
	}
}
