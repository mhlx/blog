package me.qyh.blog.web.front.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.WebUtils;

import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.exception.InvalidCaptchaException;
import me.qyh.blog.security.CaptchaValidator;

@Controller
public class UnlockFrontController {

	private final CaptchaValidator captchaValidator;

	public UnlockFrontController(CaptchaValidator captchaValidator) {
		super();
		this.captchaValidator = captchaValidator;
	}

	@PostMapping("unlock")
	public String unlock(@RequestParam("password") String password, @RequestParam("url") String url,
			@RequestParam("id") String id, HttpServletRequest request, Model model) {
		try {
			captchaValidator.validate(request);
		} catch (InvalidCaptchaException e) {
			model.addAttribute("url", url);
			model.addAttribute("id", id);
			model.addAttribute("error", new Message("captcha.invalid", "验证码错误"));
			return "unlock";
		}
		HttpSession session = request.getSession(true);
		synchronized (WebUtils.getSessionMutex(session)) {
			@SuppressWarnings("unchecked")
			Map<String, String> passwordMap = (Map<String, String>) session
					.getAttribute(Constants.PASSWORD_SESSION_KEY);
			if (passwordMap == null) {
				passwordMap = new HashMap<>();
			}
			passwordMap.put(id, password);
			session.setAttribute(Constants.PASSWORD_SESSION_KEY, passwordMap);
		}
		return "redirect:" + url;
	}

}
