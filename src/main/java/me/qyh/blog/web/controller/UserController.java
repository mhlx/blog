package me.qyh.blog.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.BlogConfigService;
import me.qyh.blog.vo.User;

@Authenticated
@RestController
@RequestMapping("api")
public class UserController {

	private final BlogConfigService blogConfigService;

	public UserController(BlogConfigService blogConfigService) {
		super();
		this.blogConfigService = blogConfigService;
	}

	@GetMapping("user")
	public User getUser() {
		return blogConfigService.getUser();
	}
}