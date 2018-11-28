package me.qyh.blog.plugin.comment.entity;

import me.qyh.blog.core.message.Message;

/**
 * 展现方式
 * 
 * @author Administrator
 *
 */
public enum CommentMode {
	LIST(new Message("article.commentMode.list", "平铺")), TREE(new Message("article.commentMode.tree", "嵌套"));

	private Message message;

	CommentMode(Message message) {
		this.message = message;
	}

	CommentMode() {

	}

	public Message getMessage() {
		return message;
	}
}
