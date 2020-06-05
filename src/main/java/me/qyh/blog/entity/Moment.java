package me.qyh.blog.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import me.qyh.blog.EmptyStringToNullDeserializer;
import me.qyh.blog.security.PasswordProtect;
import me.qyh.blog.security.PrivateProtect;

public class Moment implements Serializable, PasswordProtect, PrivateProtect {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	@NotBlank(message = "动态内容不能为空")
	@Size(max = 2000, message = "动态内容不能超过2000个字符")
	private String content;
	@NotNull(message = "动态日期不能为空")
	private LocalDateTime time;
	private LocalDateTime modifyTime;
	@NotNull(message = "动态是否只允许私人访问不能为空")
	private Boolean isPrivate;
	@NotNull(message = "动态是否允许评论不能为空")
	private Boolean allowComment;
	@JsonProperty(access = Access.WRITE_ONLY)
	@Size(max = 25, message = "动态密码长度不能超过25个字符")
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String password;
	private Integer hits;
	private Integer comments;
	/**
	 * @since 9.0
	 */
	private String firstImage;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public LocalDateTime getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(LocalDateTime modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Boolean getAllowComment() {
		return allowComment;
	}

	public void setAllowComment(Boolean allowComment) {
		this.allowComment = allowComment;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getHits() {
		return hits;
	}

	public void setHits(Integer hits) {
		this.hits = hits;
	}

	public Integer getComments() {
		return comments;
	}

	public void setComments(Integer comments) {
		this.comments = comments;
	}

	@Override
	public void clearProtected() {
		if (isHasPassword()) {
			this.content = null;
			this.password = "";
		}
	}

	@Override
	public String getResId() {
		return "moment-" + this.id;
	}

	public String getFirstImage() {
		return firstImage;
	}

	public void setFirstImage(String firstImage) {
		this.firstImage = firstImage;
	}
}
