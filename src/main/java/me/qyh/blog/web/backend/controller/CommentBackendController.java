package me.qyh.blog.web.backend.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.entity.Comment;
import me.qyh.blog.service.CommentService;
import me.qyh.blog.utils.WebUtils;
import me.qyh.blog.vo.CommentQueryParam;
import me.qyh.blog.vo.PageResult;

@Controller
@RequestMapping("console/comments")
public class CommentBackendController {

	private final CommentService commentService;

	public CommentBackendController(CommentService commentService) {
		this.commentService = commentService;
	}

	@ResponseBody
	@PostMapping("{id}/delete")
	public void deleteComment(@PathVariable("id") int id) {
		commentService.deleteComment(id);
	}

	@ResponseBody
	@PostMapping("{id}/update")
	public void updateComment(@PathVariable("id") int id, @Valid @RequestBody Comment comment) {
		commentService.updateContent(id, comment.getContent());
	}

	@ResponseBody
	@PostMapping("{id}/check")
	public void checkComment(@PathVariable("id") int id) {
		commentService.checkComment(id);
	}

	@ResponseBody
	@GetMapping(headers = WebUtils.AJAX_HEADER)
	public PageResult<Comment> param(@Valid CommentQueryParam param) {
		if (!param.hasPageSize()) {
			param.setPageSize(10);
		}
		return commentService.queryComments(param);
	}

	@GetMapping
	public String index() {
		return "console/comment/index";
	}
}
