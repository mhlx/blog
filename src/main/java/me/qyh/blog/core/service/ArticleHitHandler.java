package me.qyh.blog.core.service;

import me.qyh.blog.core.entity.Article;

/**
 * 文章被点击后的处理器
 * 
 * @author wwwqyhme
 *
 */
public interface ArticleHitHandler {

	/**
	 * @param article
	 */
	void hit(Article article);

}
