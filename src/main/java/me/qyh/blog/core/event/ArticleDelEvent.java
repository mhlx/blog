package me.qyh.blog.core.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Article;

/**
 * 用户逻辑删除|实际删除文章时触发
 * 
 * @author wwwqyhme
 *
 */
public class ArticleDelEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Article> articles;
	private final boolean logicDelete;

	public ArticleDelEvent(Object source, List<Article> articles, boolean logicDelete) {
		super(source);
		this.articles = articles;
		this.logicDelete = logicDelete;
	}

	public List<Article> getArticles() {
		return articles;
	}

	public boolean isLogicDelete() {
		return logicDelete;
	}

}
