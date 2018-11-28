package me.qyh.blog.plugin.comment.service;

import java.util.concurrent.TimeUnit;

import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.vo.Limit;

/**
 * 评论配置
 * 
 * @author Administrator
 *
 */
public class CommentConfig {

	private Editor editor;// 编辑器
	private Integer limitCount;
	private Integer limitSec;
	private Boolean check;// 审核
	private int pageSize;
	private String nickname;// 管理员的昵称

	public CommentConfig() {
		super();
	}

	public CommentConfig(CommentConfig source) {
		this.editor = source.editor;
		this.check = source.check;
		this.limitCount = source.limitCount;
		this.limitSec = source.limitSec;
		this.pageSize = source.pageSize;
		this.nickname = source.nickname;
	}

	public Integer getLimitSec() {
		return limitSec;
	}

	public void setLimitSec(Integer limitSec) {
		this.limitSec = limitSec;
	}

	public Integer getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(Integer limitCount) {
		this.limitCount = limitCount;
	}

	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	public Editor getEditor() {
		return editor;
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

	public Limit getLimit() {
		return new Limit(limitCount, limitSec, TimeUnit.SECONDS);
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
