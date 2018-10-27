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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.service.CommentConfig;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.validator.CommentConfigValidator;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;
import me.qyh.blog.plugin.comment.vo.PeriodCommentQueryParam;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RequestMapping("api/console")
@RestController
@EnsureLogin
public class CommentConsole extends BaseMgrController {

	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentConfigValidator commentConfigValidator;

	@InitBinder(value = "commentConfig")
	protected void initCommentConfigBinder(WebDataBinder binder) {
		binder.setValidator(commentConfigValidator);
	}

	@DeleteMapping(value = "comment/{id}")
	public JsonResult remove(@PathVariable("id") Integer id) throws LogicException {
		commentService.deleteComment(id);
		return new JsonResult(true, new Message("comment.delete.success", "删除成功"));
	}

	// TODO
	@PostMapping(value = "ban", params = { "id" })
	@ResponseBody
	public JsonResult ban(@RequestParam("id") Integer id) throws LogicException {
		commentService.banIp(id);
		return new JsonResult(true, new Message("comment.ban.success", "禁止成功"));
	}

	// TODO
	@PostMapping(value = "blacklist")
	@ResponseBody
	public JsonResult removeBan(@RequestParam("ip") String ip) throws LogicException {
		commentService.removeBan(ip);
		return new JsonResult(true, new Message("comment.removeBan.success", "删除成功"));
	}

	@GetMapping("comment/blacklist")
	public PageResult<String> blacklist(IPQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(Constants.DEFAULT_PAGE_SIZE);
		return commentService.queryBlacklist(param);
	}

	@PatchMapping("comment/{id}")
	public ResponseEntity<Void> check(@PathVariable("id") Integer id) throws LogicException {
		commentService.checkComment(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("commentConfig")
	public ResponseEntity<Void> update(@RequestBody @Validated CommentConfig commentConfig) {
		commentService.updateCommentConfig(commentConfig);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("uncheckedComments")
	public PageResult<Comment> uncheck(PageQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(commentService.getCommentConfig().getPageSize());
		return commentService.queryUncheckComments(param);
	}

	@GetMapping("comments")
	public PageResult<Comment> queryAll(PeriodCommentQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(commentService.getCommentConfig().getPageSize());
		return commentService.queryAllCommentsByPeriod(param);
	}

	@GetMapping("uncheckedCommentsCount")
	public int uncheckCount() {
		return commentService.queryUncheckCommentCount();
	}

}
