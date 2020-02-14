package me.qyh.blog.web.backend.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.security.TOTPAuthenticator;
import me.qyh.blog.service.BlogConfigService;
import me.qyh.blog.web.AjaxError;

@Controller
@RequestMapping("console/config")
public class BlogConfigBackendController {

	private final BlogConfigService configService;
	private final TOTPAuthenticator authenticator;

	public BlogConfigBackendController(BlogConfigService configService, @Nullable TOTPAuthenticator authenticator) {
		super();
		this.configService = configService;
		this.authenticator = authenticator;
	}

	@GetMapping
	public String index(Model model) {
		model.addAttribute("config", configService.getConfig());
		return "console/config/index";
	}

	@PostMapping("save")
	@ResponseBody
	public ResponseEntity<Object> save(@RequestBody @Valid BlogConfig config,
			@RequestParam("oldPassword") String oldPassword,
			@RequestParam(value = "totpCode", required = false) String totpCode, HttpSession session) {
		if (authenticator != null && !authenticator.check(totpCode)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AjaxError(new Message("otp.authFail", "二次认证失败")));
		}
		configService.update(config, oldPassword);
		session.setAttribute(Constants.AUTH_SESSION_KEY, configService.getUser());
		return ResponseEntity.noContent().build();
	}
}
