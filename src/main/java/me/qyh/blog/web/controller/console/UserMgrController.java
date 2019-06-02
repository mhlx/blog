package me.qyh.blog.web.controller.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.qyh.blog.core.security.LoginAuthenticator;

@Controller
@RequestMapping("console/user")
public class UserMgrController extends BaseMgrController {

	@Autowired
	private LoginAuthenticator authenticator;

	@GetMapping
	public String index(Model model) {
		model.addAttribute("otpRequired", authenticator.enable());
		return "console/user/index";
	}

}
