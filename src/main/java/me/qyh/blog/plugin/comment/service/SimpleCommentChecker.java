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
		if (Environment.isLogin()) {
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
