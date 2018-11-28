package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Article;

/**
 * 用户新增文章时将会触发该时间
 * 
 * @author wwwqyhme
 *
 */
public class ArticleCreateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Article article;

	public ArticleCreateEvent(Object source, Article article) {
		super(source);
		this.article = article;
	}

	public Article getArticle() {
		return article;
	}

}
