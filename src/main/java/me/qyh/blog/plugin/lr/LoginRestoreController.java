package me.qyh.blog.plugin.lr;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.vo.ExportPage;
import me.qyh.blog.template.vo.ExportPages;
import me.qyh.blog.web.security.CaptchaValidator;

@Controller
public class LoginRestoreController {

	@Autowired
	private CaptchaValidator captchaValidator;
	@Autowired
	private GoogleAuthenticator ga;
	@Autowired
	private TemplateService templateService;

	private final String loginTemplate;

	private final AttemptLogger attemptLogger;

	public LoginRestoreController(AttemptLogger attemptLogger) throws IOException {
		super();
		this.attemptLogger = attemptLogger;
		this.loginTemplate = Resources.readResourceToString(new ClassPathResource("resources/page/LOGIN.html"));
	}

	@PostMapping("login/restore")
	@ResponseBody
	public JsonResult restore(@RequestParam("code") String codeStr, HttpServletRequest request,
			HttpServletResponse response) throws LogicException {
		if (Environment.isLogin()) {
			return new JsonResult(false, new Message("login.restore.isLogin", "当前已经是登录状态，请直接修改页面"));
		}
		String ip = Environment.getIP();
		if (attemptLogger.log(ip)) {
			captchaValidator.doValidate(request);
		}
		if (!ga.checkCode(codeStr)) {
			return new JsonResult(false, new Message("otp.verifyFail", "动态口令校验失败"));
		}

		ExportPage exportPage = new ExportPage();
		Page loginPage = new Page();
		loginPage.setAlias("login");
		loginPage.setTpl(loginTemplate);
		loginPage.setName("login");
		exportPage.setPage(loginPage);

		ExportPages exportPages = new ExportPages();
		exportPages.setPages(List.of(exportPage));
		templateService.importPage(exportPages);

		attemptLogger.remove(Environment.getIP());
		return new JsonResult(true, new Message("login.restore.success", "恢复成功"));
	}

	@GetMapping("login/restore")
	public String restore() {
		if (Environment.isLogin()) {
			return "redirect:/mgr/template/page/index";
		}
		return "plugin/lr/login_restore";
	}

}
