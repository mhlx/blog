package me.qyh.blog.plugin.comment.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentService;

@Controller
public class CommentController {

	@Autowired
	private CommentService commentService;
	@Autowired
	private UrlHelper urlHelper;

	@EnsureLogin
	@GetMapping("console/comment")
	public String index() {
		return "plugin/comment/all";
	}

	@EnsureLogin
	@GetMapping("console/comment/blacklist")
	public String blacklist() {
		return "plugin/comment/blacklist";
	}

	@EnsureLogin
	@GetMapping("console/comment/config")
	public String config(Model model) {
		model.addAttribute("config", commentService.getCommentConfig());
		return "plugin/comment/config";
	}

	@GetMapping("comment/link/{module}/{id}")
	public String getCommentUrl(@PathVariable("module") String module, @PathVariable("id") Integer id) {
		return "redirect:"
				+ commentService.queryCommentModuleUrl(new CommentModule(module, id)).orElse(urlHelper.getUrl());
	}

}
