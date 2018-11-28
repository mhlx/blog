package me.qyh.blog.web.controller.console;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("console/config")
@Controller
public class ConfigMgrController extends BaseMgrController {

	@GetMapping
	public String index() {
		return "console/config/index";
	}

}
