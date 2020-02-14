package me.qyh.blog.web.front.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.service.CommentService;
import me.qyh.blog.service.MomentService;
import me.qyh.blog.utils.WebUtils;
import me.qyh.blog.vo.CommentQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.SavedComment;

@Controller
public class FrontController {

	private final CommentService commentService;
	private final Bucket commentBucket;
	private final CaptchaValidator captchaValidator;
	private final ArticleService articleService;
	private final MomentService momentService;

	public FrontController(ArticleService articleService, MomentService momentService, CommentService commentService,
			BlogProperties blogProperties, CaptchaValidator captchaValidator) {
		this.commentService = commentService;
		this.commentBucket = blogProperties.getCommentBucket();
		this.captchaValidator = captchaValidator;
		this.articleService = articleService;
		this.momentService = momentService;
	}

	@PostMapping("articles/{id}/hit")
	@ResponseBody
	public void hitArticle(@PathVariable("id") int id, HttpServletRequest request) {
		if (WebUtils.isSpider(request)) {
			return;
		}
		articleService.hit(id);
	}

	@PostMapping("moments/{id}/hit")
	@ResponseBody
	public void hitMoment(@PathVariable("id") int id, HttpServletRequest request) {
		if (WebUtils.isSpider(request)) {
			return;
		}
		momentService.hit(id);
	}

	@ResponseBody
	@GetMapping("comments/{id}/conversation")
	public List<Comment> getCommentConversation(@PathVariable("id") int id) {
		return commentService.getCommentConversation(id);
	}

	@ResponseBody
	@GetMapping("comments/{id}")
	public Comment getComment(@PathVariable("id") int id) {
		return commentService.getComment(id)
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
	}

	@ResponseBody
	@PostMapping("commentModule/{name}/{id}/comment/add")
	public SavedComment comment(@PathVariable("name") String name, @PathVariable("id") int id,
			@RequestBody @Valid Comment comment, HttpServletRequest request) {
		if (!BlogContext.isAuthenticated() && !commentBucket.tryConsume(1)) {
			captchaValidator.validate(request);
		}
		comment.setModule(new CommentModule(name, id));
		comment.setIp(BlogContext.getIP().orElseThrow());
		return commentService.saveComment(comment);
	}

	@ResponseBody
	@GetMapping("commentModule/{name}/{id}/comments")
	public PageResult<Comment> queryComment(@PathVariable("name") String name, @PathVariable("id") int id,
			@Valid CommentQueryParam param) {
		param.setParent(null);
		param.setModule(new CommentModule(name, id));
		if (!BlogContext.isAuthenticated()) {
			param.setPageSize(10);
		}
		return commentService.queryComments(param);
	}

	@ResponseBody
	@GetMapping("commentModule/{name}/{id}/comments/{parent}/comments")
	public PageResult<Comment> queryComment2(@PathVariable("name") String name, @PathVariable("id") int id,
			@PathVariable("parent") int parent, @Valid CommentQueryParam param) {
		param.setParent(parent);
		param.setModule(new CommentModule(name, id));
		return commentService.queryComments(param);
	}
}
