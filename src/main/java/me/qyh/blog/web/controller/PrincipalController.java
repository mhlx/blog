package me.qyh.blog.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.BlogContext;

@Controller
public class PrincipalController {

	@ResponseBody
	@GetMapping("isAuthenticated")
	public boolean isAuthenticated() {
		return BlogContext.isAuthenticated();
	}

}
