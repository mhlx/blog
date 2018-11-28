package me.qyh.blog.plugin.comment.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.AttemptLoggerManager;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentConfig;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.validator.CommentConfigValidator;
import me.qyh.blog.plugin.comment.validator.CommentValidator;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;
import me.qyh.blog.plugin.comment.vo.PeriodCommentQueryParam;
import me.qyh.blog.web.security.CaptchaValidator;

@RestController
public class CommentApi implements InitializingBean {

	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentValidator commentValidator;
	@Autowired
	private CaptchaValidator captchaValidator;
	@Autowired
	private AttemptLoggerManager attemptLoggerManager;

	@Autowired
	private CommentConfigValidator commentConfigValidator;

	@InitBinder(value = "commentConfig")
	protected void initCommentConfigBinder(WebDataBinder binder) {
		binder.setValidator(commentConfigValidator);
	}

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

	@GetMapping("api/commentConfig")
	public CommentConfig getConfig() {
		return commentService.getCommentConfig();
	}

	@PostMapping({ "space/{alias}/api/{type}/{id}/comment", "api/{type}/{id}/comment" })
	public ResponseEntity<Comment> addComment(@RequestBody @Validated Comment comment,
			@PathVariable("type") String type, @PathVariable("id") Integer moduleId, HttpServletRequest req)
			throws LogicException {
		if (!Environment.hasAuthencated() && attemptLogger.log(Environment.getIP())) {
			captchaValidator.doValidate(req);
		}
		comment.setCommentModule(new CommentModule(type, moduleId));
		comment.setIp(Environment.getIP());
		Comment returned = commentService.insertComment(comment);
		return ResponseEntity.status(HttpStatus.CREATED).body(returned);
	}

	@GetMapping({ "space/{alias}/api/{type}/{id}/comment/{commentId}/conversation",
			"api/{type}/{id}/comment/{commentId}/conversation" })
	public List<Comment> queryConversations(@PathVariable("type") String type, @PathVariable("id") Integer moduleId,
			@PathVariable("commentId") Integer commentId) throws LogicException {
		return commentService.queryConversations(new CommentModule(type, moduleId), commentId);
	}

	@GetMapping("api/comment/captchaRequirement")
	public boolean needCaptcha() {
		return !Environment.hasAuthencated() && attemptLogger.reach(Environment.getIP());
	}

	@EnsureLogin
	@DeleteMapping("api/console/comment/{id}")
	public ResponseEntity<Void> remove(@PathVariable("id") Integer id) throws LogicException {
		commentService.deleteComment(id);
		return ResponseEntity.noContent().build();
	}

	@EnsureLogin
	@PostMapping(value = "api/console/comment/blacklistItem")
	public ResponseEntity<Void> ban(@RequestParam("id") Integer id) throws LogicException {
		commentService.banIp(id);
		return ResponseEntity.noContent().build();
	}

	@EnsureLogin
	@DeleteMapping(value = "api/console/comment/blacklistItem/{ip:.+}")
	public ResponseEntity<Void> removeBan(@PathVariable("ip") String ip) throws LogicException {
		commentService.removeBan(ip);
		return ResponseEntity.noContent().build();
	}

	@EnsureLogin
	@GetMapping("api/console/comment/blacklist")
	public PageResult<String> blacklist(IPQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(Constants.DEFAULT_PAGE_SIZE);
		return commentService.queryBlacklist(param);
	}

	@EnsureLogin
	@PatchMapping("api/console/comment/{id}")
	public ResponseEntity<Void> check(@PathVariable("id") Integer id, @RequestParam("status") CommentStatus status)
			throws LogicException {
		commentService.changeStatus(id, status);
		return ResponseEntity.noContent().build();
	}

	@EnsureLogin
	@PutMapping("api/console/commentConfig")
	public ResponseEntity<Void> update(@RequestBody @Validated CommentConfig commentConfig) {
		commentService.updateCommentConfig(commentConfig);
		return ResponseEntity.noContent().build();
	}

	@EnsureLogin
	@GetMapping("api/console/comments")
	public PageResult<Comment> queryAll(PeriodCommentQueryParam param) {
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		param.setPageSize(commentService.getCommentConfig().getPageSize());
		return commentService.queryAllCommentsByPeriod(param);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.attemptLogger = attemptLoggerManager.createAttemptLogger(attemptCount, maxAttemptCount, sleepSec);
	}
}
