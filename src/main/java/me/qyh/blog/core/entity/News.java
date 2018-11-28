package me.qyh.blog.core.entity;

import java.sql.Timestamp;

/**
 * 动态
 * 
 * @since 6.0
 * @author wwwqyhme
 *
 */
public class News extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String content;// 内容 HTML格式
	private Timestamp write;// 撰写日期
	private Timestamp update;// 更新日期
	private Boolean isPrivate;
	private int comments;// 评论数目
	private Boolean allowComment;

	/**
	 * @since 6.4
	 */
	private int hits;
	/**
	 * @since 6.6
	 */
	private String lockId;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getWrite() {
		return write;
	}

	public void setWrite(Timestamp write) {
		this.write = write;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public Timestamp getUpdate() {
		return update;
	}

	public void setUpdate(Timestamp update) {
		this.update = update;
	}

	public Boolean getAllowComment() {
		return allowComment;
	}

	public void setAllowComment(Boolean allowComment) {
		this.allowComment = allowComment;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public String getLockId() {
		return lockId;
	}

	public void setLockId(String lockId) {
		this.lockId = lockId;
	}

	public boolean hasLock() {
		return lockId != null;
	}

}
