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
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.plugin.comment.service.CommentConfig;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.validator.CommentConfigValidator;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;
import me.qyh.blog.plugin.comment.vo.PeriodCommentQueryParam;
import me.qyh.blog.web.controller.back.BaseMgrController;

@RequestMapping("mgr/comment")
@Controller
public class CommentMgrController extends BaseMgrController {

	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentConfigValidator commentConfigValidator;

	@InitBinder(value = "commentConfig")
	protected void initCommentConfigBinder(WebDataBinder binder) {
		binder.setValidator(commentConfigValidator);
	}

	@PostMapping(value = "delete", params = { "id" })
	@ResponseBody
	public JsonResult remove(@RequestParam("id") Integer id) throws LogicException {
		commentService.deleteComment(id);
		return new JsonResult(true, new Message("comment.delete.success", "删除成功"));
	}

	@PostMapping(value = "ban", params = { "id" })
	@ResponseBody
	public JsonResult ban(@RequestParam("id") Integer id) throws LogicException {
		commentService.banIp(id);
		return new JsonResult(true, new Message("comment.ban.success", "禁止成功"));
	}

	@PostMapping(value = "removeBan")
	@ResponseBody
	public JsonResult removeBan(@RequestParam("ip") String ip) throws LogicException {
		commentService.removeBan(ip);
		return new JsonResult(true, new Message("comment.removeBan.success", "删除成功"));
	}

	@GetMapping("blacklist")
	@ResponseBody
	public JsonResult blacklist(IPQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(Constants.DEFAULT_PAGE_SIZE);
		return new JsonResult(true, commentService.queryBlacklist(param));
	}

	@PostMapping("check")
	@ResponseBody
	public JsonResult check(@RequestParam("id") Integer id) throws LogicException {
		commentService.checkComment(id);
		return new JsonResult(true, new Message("comment.check.success", "审核成功"));
	}

	@GetMapping("updateConfig")
	public String update(ModelMap model) {
		model.addAttribute("config", commentService.getCommentConfig());
		return "plugin/comment/config";
	}

	@PostMapping("updateConfig")
	@ResponseBody
	public JsonResult update(@RequestBody @Validated CommentConfig commentConfig) {
		commentService.updateCommentConfig(commentConfig);
		return new JsonResult(true, new Message("comment.config.update.success", "更新成功"));
	}

	@GetMapping("uncheck")
	public String uncheck(PageQueryParam param, Model model) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(commentService.getCommentConfig().getPageSize());
		model.addAttribute("page", commentService.queryUncheckComments(param));
		return "plugin/comment/uncheck";
	}

	@GetMapping("all")
	public String queryAll(PeriodCommentQueryParam param, Model model) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(commentService.getCommentConfig().getPageSize());
		model.addAttribute("page", commentService.queryAllCommentsByPeriod(param));
		return "plugin/comment/all";
	}

	@GetMapping("uncheckCount")
	@ResponseBody
	public JsonResult uncheckCount() {
		return new JsonResult(true, commentService.queryUncheckCommentCount());
	}

}
