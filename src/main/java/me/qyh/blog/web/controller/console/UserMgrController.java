package me.qyh.blog.web.controller.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.qyh.blog.core.security.GoogleAuthenticator;

@Controller
@RequestMapping("console/user")
public class UserMgrController extends BaseMgrController {

	@Autowired(required = false)
	private GoogleAuthenticator ga;

	@GetMapping
	public String index(Model model) {
		model.addAttribute("otpRequired", ga != null);
		return "console/user/index";
	}

}
