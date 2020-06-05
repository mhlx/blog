package me.qyh.blog.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.service.CommentService;
import me.qyh.blog.vo.CommentQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.SavedComment;
import me.qyh.blog.web.template.TemplateDataMapping;

@RestController
@RequestMapping("api")
public class CommentController {

	private final CommentService commentService;
	private final Bucket commentBucket;
	private final CaptchaValidator captchaValidator;
	private final BlogProperties blogProperties;

	public CommentController(CommentService commentService, CaptchaValidator captchaValidator,
			BlogProperties blogProperties) {
		super();
		this.commentService = commentService;
		this.commentBucket = blogProperties.getCommentBucket();
		this.captchaValidator = captchaValidator;
		this.blogProperties = blogProperties;
	}

	@TemplateDataMapping("commentModule/{name}/{moduleId}/comments/{id}/conversation")
	public List<Comment> getCommentConversation(@PathVariable("name") String name,
			@PathVariable("moduleId") int moduleId, @PathVariable("id") int id) {
		return commentService.getCommentConversation(id, new CommentModule(name, moduleId));
	}

	@TemplateDataMapping("commentModule/{name}/{moduleId}/comments/{id}")
	public Comment getComment(@PathVariable("name") String name, @PathVariable("moduleId") int moduleId,
			@PathVariable("id") int id) {
		return commentService.getComment(id, new CommentModule(name, moduleId))
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
	}

	@GetMapping("editableComments/{id}")
	public Comment getCommentForEdit(@PathVariable("id") int id) {
		return commentService.getCommentForEdit(id)
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
	}

	@TemplateDataMapping("commentModule/{name}/{id}/comments")
	public PageResult<Comment> queryComment(@PathVariable("name") String name, @PathVariable("id") int id,
			@Valid CommentQueryParam param) {
		param.setParent(null);
		param.setModule(new CommentModule(name, id));
		return commentService.queryComments(param);
	}

	@TemplateDataMapping("commentModule/{name}/{id}/comments/{parent}/comments")
	public PageResult<Comment> queryComment2(@PathVariable("name") String name, @PathVariable("id") int id,
			@PathVariable("parent") int parent, @Valid CommentQueryParam param) {
		param.setParent(parent);
		param.setModule(new CommentModule(name, id));
		return commentService.queryComments(param);
	}

	@PostMapping("commentModule/{name}/{id}/comment")
	public ResponseEntity<SavedComment> comment(@PathVariable("name") String name, @PathVariable("id") int id,
			@RequestBody @Valid Comment comment, @RequestParam(name = "captcha_key", required = false) String captchKey,
			@RequestParam(name = "captcha_value", required = false) String captchaValue) {
		if (!BlogContext.isAuthenticated() && !commentBucket.tryConsume(1)) {
			captchaValidator.validate(captchKey, captchaValue);
		}
		comment.setModule(new CommentModule(name, id));
		comment.setIp(BlogContext.getIP().orElseThrow());
		SavedComment savedComment = commentService.saveComment(comment);
		return ResponseEntity.created(blogProperties.buildUrl("api/comments/" + savedComment.getId()))
				.body(savedComment);
	}

	@Authenticated
	@DeleteMapping("comments/{id}")
	public ResponseEntity<?> deleteComment(@PathVariable("id") int id) {
		commentService.deleteComment(id);
		return ResponseEntity.noContent().build();
	}

	@Authenticated
	@PatchMapping(value = "comments/{id}", params = { "content" })
	public ResponseEntity<?> updateComment(@PathVariable("id") int id, @RequestParam("content") String content) {
		commentService.updateContent(id, content);
		return ResponseEntity.noContent().build();
	}

	@Authenticated
	@PatchMapping(value = "comments/{id}", params = { "checking" })
	public ResponseEntity<?> updateComment(@PathVariable("id") int id, @RequestParam("checking") boolean checking) {
		commentService.checkComment(id, checking);
		return ResponseEntity.noContent().build();
	}

	@Authenticated
	@GetMapping("comments")
	public PageResult<Comment> param(@Valid CommentQueryParam param) {
		param.setModule(null);
		return commentService.queryComments(param);
	}

	@TemplateDataMapping("commentModule/{name}/{id}")
	public Object getModuleTarget(@PathVariable("name") String name, @PathVariable("id") int id) {
		return commentService.getModuleTarget(new CommentModule(name, id));
	}

	@Authenticated
	@TemplateDataMapping("lastComments")
	public List<Comment> param(@RequestParam(value = "num", required = false, defaultValue = "5") int num,
			@RequestParam(value = "queryAdmin", required = false, defaultValue = "false") boolean queryAdmin) {
		return commentService.getLastComments(num, queryAdmin);
	}

}
