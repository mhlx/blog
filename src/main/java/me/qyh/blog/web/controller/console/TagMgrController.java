package me.qyh.blog.web.controller.console;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("console/tag")
@Controller
public class TagMgrController extends BaseMgrController {

	@GetMapping
	public String index() {
		return "console/tag/index";
	}

}
