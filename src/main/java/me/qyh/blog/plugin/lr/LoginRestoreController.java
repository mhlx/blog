package me.qyh.blog.plugin.lr;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.web.security.CaptchaValidator;

@Controller
public class LoginRestoreController {

	@Autowired
	private CaptchaValidator captchaValidator;
	@Autowired
	private GoogleAuthenticator ga;
	@Autowired
	private TemplateService templateService;

	private final AttemptLogger attemptLogger;

	public LoginRestoreController(AttemptLogger attemptLogger) throws IOException {
		super();
		this.attemptLogger = attemptLogger;
	}

	@PostMapping("login/restore")
	public String restore(@RequestParam("code") String codeStr, HttpServletRequest request, RedirectAttributes ra)
			throws LogicException {
		if (Environment.hasAuthencated()) {
			return "redirect:/console/template/page";
		}
		String ip = Environment.getIP();
		if (attemptLogger.log(ip)) {
			ra.addFlashAttribute("captchaRequired", true);
			try {
				captchaValidator.doValidate(request);
			} catch (LogicException e) {
				ra.addFlashAttribute("error", e.getLogicMessage());
				return "redirect:/login/restore";
			}
		}
		if (!ga.checkCode(codeStr)) {
			ra.addFlashAttribute("error", new Message("otp.verifyFail", "动态口令校验失败"));
			return "redirect:/login/restore";
		}
		templateService.disablePageByPath("login");
		attemptLogger.remove(Environment.getIP());
		return "redirect:/login";
	}

	@GetMapping("login/restore")
	public String restore() {
		if (Environment.hasAuthencated()) {
			return "redirect:/console/template/page";
		}
		return "plugin/lr/login_restore";
	}

}
