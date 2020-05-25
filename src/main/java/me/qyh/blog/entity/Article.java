package me.qyh.blog.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import me.qyh.blog.EmptyStringToNullDeserializer;
import me.qyh.blog.security.PasswordProtect;
import me.qyh.blog.security.PrivateProtect;

public class Article implements Serializable, PrivateProtect, PasswordProtect {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ArticleStatus {
		PUBLISHED, // 发布的
		DRAFT, // 草稿
		SCHEDULED;// 计划发表中
	}

	private Integer id;
	private String title;
	private String content;
	private LocalDateTime pubDate;
	private LocalDateTime lastModifyDate;
	private Boolean isPrivate;
	private Integer hits;
	private Integer comments;
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String summary;
	private Integer level;
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String alias;
	private Boolean allowComment;
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String password;
	private ArticleStatus status;
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<Tag> tags = new LinkedHashSet<>();
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<Category> categories = new LinkedHashSet<>();
	@JsonDeserialize(using = EmptyStringToNullDeserializer.class)
	private String featureImage;

	public ArticleStatus getStatus() {
		return status;
	}

	public void setStatus(ArticleStatus status) {
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getPubDate() {
		return pubDate;
	}

	public void setPubDate(LocalDateTime pubDate) {
		this.pubDate = pubDate;
	}

	public LocalDateTime getLastModifyDate() {
		return lastModifyDate;
	}

	public void setLastModifyDate(LocalDateTime lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public String getFeatureImage() {
		return featureImage;
	}

	public void setFeatureImage(String featureImage) {
		this.featureImage = featureImage;
	}

	@Override
	public void clearProtected() {
		if (!this.isHasPassword()) {
			return;
		}
		this.content = null;
		this.summary = null;
		this.password = "";
	}

	@Override
	public String getResId() {
		return "article-" + this.id;
	}
}
