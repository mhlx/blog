package me.qyh.blog.web.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.BlogConfigService;

@RestController
@RequestMapping("api/setting")
@Authenticated
public class BlogSettingController {

	private final BlogConfigService configService;

	public BlogSettingController(BlogConfigService configService) {
		super();
		this.configService = configService;
	}

	@GetMapping
	public BlogConfig index() {
		return configService.getConfig();
	}

	@PutMapping
	public ResponseEntity<Object> save(@RequestBody @Valid BlogConfig config,
			@RequestParam("oldPassword") String oldPassword) {
		configService.update(config, oldPassword);
		return ResponseEntity.noContent().build();
	}
}
