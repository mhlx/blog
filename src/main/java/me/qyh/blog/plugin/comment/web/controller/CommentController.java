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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.AttemptLoggerManager;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.validator.CommentValidator;
import me.qyh.blog.web.security.CaptchaValidator;

@Controller
public class CommentController implements InitializingBean {

	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentValidator commentValidator;
	@Autowired
	private CaptchaValidator captchaValidator;
	@Autowired
	private AttemptLoggerManager attemptLoggerManager;
	@Autowired
	private UrlHelper urlHelper;

	@Value("${comment.attempt.count:5}")
	private int attemptCount;

	@Value("${comment.attempt.maxCount:50}")
	private int maxAttemptCount;

	@Value("${comment.attempt.sleepSec:300}")
	private int sleepSec;

	private AttemptLogger attemptLogger;

	@InitBinder(value = "comment")
	protected void initCommentBinder(WebDataBinder binder) {
		binder.setValidator(commentValidator);
	}

	@GetMapping("comment/config")
	@ResponseBody
	public JsonResult getConfig() {
		return new JsonResult(true, commentService.getCommentConfig());
	}

	@PostMapping({ "space/{alias}/{type}/{id}/addComment", "{type}/{id}/addComment" })
	@ResponseBody
	public JsonResult addComment(@RequestBody @Validated Comment comment, @PathVariable("type") String type,
			@PathVariable("id") Integer moduleId, HttpServletRequest req) throws LogicException {
		if (!Environment.isLogin() && attemptLogger.log(Environment.getIP())) {
			captchaValidator.doValidate(req);
		}
		comment.setCommentModule(new CommentModule(type, moduleId));
		comment.setIp(Environment.getIP());
		return new JsonResult(true, commentService.insertComment(comment));
	}

	@GetMapping({ "space/{alias}/{type}/{id}/comment/{commentId}/conversations",
			"{type}/{id}/comment/{commentId}/conversations" })
	@ResponseBody
	public JsonResult queryConversations(@PathVariable("type") String type, @PathVariable("id") Integer moduleId,
			@PathVariable("commentId") Integer commentId) throws LogicException {
		return new JsonResult(true, commentService.queryConversations(new CommentModule(type, moduleId), commentId));
	}

	@GetMapping("comment/link/{module}/{id}")
	public String jumpToArticle(@PathVariable("module") String module, @PathVariable("id") Integer id) {
		String url = commentService.queryCommentModuleUrl(new CommentModule(module, id)).orElse(urlHelper.getUrl());
		return "redirect:" + url;
	}

	@GetMapping("comment/needCaptcha")
	@ResponseBody
	public boolean needCaptcha() {
		return !Environment.isLogin() && attemptLogger.reach(Environment.getIP());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.attemptLogger = attemptLoggerManager.createAttemptLogger(attemptCount, maxAttemptCount, sleepSec);
	}
}
