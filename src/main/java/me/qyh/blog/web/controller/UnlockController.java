package me.qyh.blog.web.controller;

import java.util.HashMap;
import java.util.List;
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
import me.qyh.blog.exception.BadRequestException;
import me.qyh.blog.security.CaptchaValidator;

@Controller
public class UnlockController {

	private final CaptchaValidator captchaValidator;

	public UnlockController(CaptchaValidator captchaValidator) {
		super();
		this.captchaValidator = captchaValidator;
	}

	@PostMapping("unlock")
	public String unlock(@RequestParam("password") String password, @RequestParam("url") String url,
			@RequestParam("id") String id, @RequestParam("captcha_key") String captchKey,
			@RequestParam("captcha_value") String captchaValue, Model model, HttpServletRequest request) {
		try {
			captchaValidator.validate(captchKey, captchaValue);
		} catch (BadRequestException e) {
			model.addAttribute("url", url);
			model.addAttribute("id", id);
			model.addAttribute("errors", List.of(new Message("captcha.invalid", "验证码错误")));
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
