package me.qyh.blog.plugin.comment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.util.UriComponentsBuilder;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.comment.entity.Comment;

public class SimpleCommentChecker implements CommentChecker {

	private String[] disallowUsernamePatterns;
	private String[] disallowEmailPatterns;

	@Autowired
	private UserService userService;

	@Override
	public void checkComment(Comment comment, CommentConfig config) throws LogicException {
		checkCommentUser(comment, config);
		checkCommentContent(comment, config);
	}

	protected void checkCommentUser(Comment comment, CommentConfig config) throws LogicException {
		if (Environment.hasAuthencated()) {
			return;
		}
		String email = comment.getEmail();
		String name = comment.getNickname();
		String website = comment.getWebsite();
		User user = userService.getUser();
		String emailOrAdmin = user.getEmail();
		if (!Validators.isEmptyOrNull(emailOrAdmin, true) && emailOrAdmin.equals(email)) {
			throw new LogicException("comment.email.invalid", "邮箱不被允许");
		}
		if (user.getName().equalsIgnoreCase(name) || config.getNickname().equalsIgnoreCase(name)) {
			throw new LogicException("comment.nickname.invalid", "昵称不被允许");
		}
		if (disallowUsernamePatterns != null && PatternMatchUtils.simpleMatch(disallowUsernamePatterns, name.strip())) {
			throw new LogicException("comment.username.invalid", "用户名不被允许");
		}

		if (email != null && disallowEmailPatterns != null
				&& PatternMatchUtils.simpleMatch(disallowEmailPatterns, email.strip())) {
			throw new LogicException("comment.email.invalid", "邮箱不被允许");
		}
		if (website != null) {
			try {

				UriComponentsBuilder.fromHttpUrl(website).build();
			} catch (Exception e) {
				throw new LogicException("comment.website.invalid", "网址不被允许");
			}
		}
	}

	protected void checkCommentContent(Comment comment, CommentConfig config) throws LogicException {
		//
	}

	public void setDisallowUsernamePatterns(String[] disallowUsernamePatterns) {
		this.disallowUsernamePatterns = disallowUsernamePatterns;
	}

	public void setDisallowEmailPatterns(String[] disallowEmailPatterns) {
		this.disallowEmailPatterns = disallowEmailPatterns;
	}
}
