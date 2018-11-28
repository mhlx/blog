package me.qyh.blog.plugin.syslock.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.qyh.blog.web.controller.console.BaseMgrController;

@Controller
@RequestMapping("console/syslock")
public class SysLockMgrController extends BaseMgrController {

	@GetMapping
	public String index() {
		return "plugin/syslock/index";
	}

}
