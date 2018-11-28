package me.qyh.blog.core.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Article;

/**
 * 文章发布事件
 * <p>
 * 用户从草稿箱发布文章或者发布计划文章时将会触发该事件
 * </p>
 * 
 * @see ArticleCreateEvent
 * @see ArticleUpdateEvent
 * 
 * @author wwwqyhme
 *
 */
public class ArticlePublishEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Article> articles;

	public ArticlePublishEvent(Object source, List<Article> articles) {
		super(source);
		this.articles = articles;
	}

	public List<Article> getArticles() {
		return articles;
	}

}
