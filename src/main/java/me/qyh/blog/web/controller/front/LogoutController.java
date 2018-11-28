package me.qyh.blog.web.controller.front;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.web.RememberMeService;
import me.qyh.blog.web.security.csrf.CsrfTokenRepository;

@Controller
public class LogoutController {

	@Autowired(required = false)
	private CsrfTokenRepository csrfTokenRepository;
	@Autowired
	private RememberMeService rememberMeService;

	@PostMapping("logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		afterLogout(request, response);
		return "redirect:/";
	}

	@PostMapping(value = "logout", headers = "x-requested-with=XMLHttpRequest")
	@ResponseBody
	public JsonResult ajaxLogout(HttpServletRequest request, HttpServletResponse response) {
		afterLogout(request, response);
		return new JsonResult(true, new Message("logout.success", "注销成功"));
	}

	private void afterLogout(HttpServletRequest request, HttpServletResponse response) {
		if (csrfTokenRepository != null) {
			csrfTokenRepository.saveToken(null, request, response);
		}
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		Environment.setUser(null);
		rememberMeService.deleteRememberMe(request, response);
	}
}
