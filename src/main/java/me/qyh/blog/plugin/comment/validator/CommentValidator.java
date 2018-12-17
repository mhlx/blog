package me.qyh.blog.plugin.comment.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.util.UriComponentsBuilder;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.UserValidator;
import me.qyh.blog.plugin.comment.entity.Comment;

@Component
public class CommentValidator implements Validator {

	private static final int MAX_COMMENT_LENGTH = 500;
	static final int MAX_NAME_LENGTH = 8;
	private static final int MAX_WEBSITE_LENGTH = 50;

	@Override
	public boolean supports(Class<?> clazz) {
		return Comment.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Comment comment = (Comment) target;
		String content = comment.getContent();
		if (Validators.isEmptyOrNull(content, true)) {
			errors.reject("comment.content.blank", "回复内容不能为空");
			return;
		}
		if (content.length() > MAX_COMMENT_LENGTH) {
			errors.reject("comment.content.toolong", new Object[] { MAX_COMMENT_LENGTH },
					"回复的内容不能超过" + MAX_COMMENT_LENGTH + "个字符");
			return;
		}
		if (!Environment.hasAuthencated()) {
			String email = comment.getEmail();
			if (email != null) {
				email = email.strip();
				if (!email.isEmpty()) {
					if (email.length() > UserValidator.MAX_EMAIL_LENGTH) {
						errors.reject("comment.email.toolong", new Object[] { UserValidator.MAX_EMAIL_LENGTH },
								"邮箱不能超过" + UserValidator.MAX_EMAIL_LENGTH + "位");
						return;
					}
					if (!UserValidator.EMAIL_PATTERN.matcher(email).matches()) {
						errors.reject("comment.email.invalid", "邮箱不被允许");
						return;
					}
				} else {
					email = null;
				}
			}
			comment.setEmail(email);

			String finalName = validateName(comment.getNickname(), errors);
			if (errors.hasErrors()) {
				return;
			}

			comment.setNickname(finalName);

			String website = comment.getWebsite();
			if (!Validators.isEmptyOrNull(website, true)) {
				if (website.length() > MAX_WEBSITE_LENGTH) {
					errors.reject("comment.website.toolong", new Object[] { MAX_WEBSITE_LENGTH },
							"网址不能超过" + MAX_WEBSITE_LENGTH + "位");
					return;
				}
				website = website.strip();
				if (!validWebsite(website)) {
					errors.reject("comment.website.invalid", "网址不被允许");
					return;
				}
				comment.setWebsite(website);
			} else {
				comment.setWebsite(null);
			}
		}
	}

	private boolean validWebsite(String website) {
		try {
			UriComponentsBuilder.fromHttpUrl(website).build();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static String validateName(String name, Errors errors) {
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("comment.nickname.blank", "昵称不能为空");
			return null;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("comment.nickname.toolong", new Object[] { MAX_NAME_LENGTH },
					"昵称不能超过" + MAX_NAME_LENGTH + "位");
			return null;
		}
		String finalName = StringUtils.removeAllSpace(name);
		if (finalName.isEmpty()) {
			errors.reject("comment.nickname.blank", "昵称不能为空");
			return null;
		}
		return finalName;
	}
}
