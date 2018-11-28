package me.qyh.blog.web.controller.console;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("console/file")
public class FileMgrController extends BaseMgrController {

	@GetMapping
	public String index() {
		return "console/file/index";
	}
}
