package me.qyh.blog.entity;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import me.qyh.blog.EmptyStringToNullDeserializer;

public class BlogConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum CommentCheckStrategy {
		FIRST_COMMENT, // ip第一次评论，需要审核
		ALWALYS, //
		NEVER
	}

	@NotBlank(message = "用户登录名不能为空")
	@Size(max = 20, message = "用户登录名长度应该不能超过25个字符")
	private String loginName;
	@Size(max = 25, message = "密码长度不能超过25个字符")
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String password;
	@Email(message = "邮箱校验失败")
	@Size(max = 100, message = "邮箱长度不能超过100个字符")
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String email;
	@Size(max = 20, message = "昵称长度不能超过20个字符")
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String nickname;

	/**
	 * gravatar头像 值为email的md5值，如果email为空，那么gravatar也为空
	 */
	private String gravatar;

	@NotNull(message = "评论审核策略不能为空")
	private CommentCheckStrategy commentCheckStrategy;

	public BlogConfig() {
		super();
	}

	public BlogConfig(BlogConfig copy) {
		this.commentCheckStrategy = copy.commentCheckStrategy;
		this.email = copy.email;
		this.gravatar = copy.gravatar;
		this.loginName = copy.loginName;
		this.nickname = copy.nickname;
		this.password = copy.password;
	}

	public CommentCheckStrategy getCommentCheckStrategy() {
		return commentCheckStrategy;
	}

	public void setCommentCheckStrategy(CommentCheckStrategy commentCheckStrategy) {
		this.commentCheckStrategy = commentCheckStrategy;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGravatar() {
		return gravatar;
	}

	public void setGravatar(String gravatar) {
		this.gravatar = gravatar;
	}

}
