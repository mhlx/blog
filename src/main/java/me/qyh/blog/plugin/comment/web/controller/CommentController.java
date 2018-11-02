/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
