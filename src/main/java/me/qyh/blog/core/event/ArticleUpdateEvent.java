package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Article;

/**
 * 用户更新文章|从回收站恢复文章时触发
 * 
 * @author wwwqyhme
 *
 */
public class ArticleUpdateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Article oldArticle;
	private final Article newArticle;

	public ArticleUpdateEvent(Object source, Article oldArticle, Article newArticle) {
		super(source);
		this.oldArticle = oldArticle;
		this.newArticle = newArticle;
	}

	public Article getOldArticle() {
		return oldArticle;
	}

	public Article getNewArticle() {
		return newArticle;
	}

}
