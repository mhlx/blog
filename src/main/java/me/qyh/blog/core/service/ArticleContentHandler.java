package me.qyh.blog.core.service;

import me.qyh.blog.core.service.impl.ArticleIndexer;
import me.qyh.blog.core.service.impl.ArticleServiceImpl;

/**
 * 文章内容处理器，用于文章内容的调整,<b>同时也将用于构建索引时文章内容的预处理</b>
 * <p>
 * 可以为空
 * </p>
 * 
 * @see ArticleIndexer
 * @see ArticleServiceImpl
 * 
 * @author Administrator
 *
 */
public interface ArticleContentHandler {
	/**
	 * 用来处理文章
	 * 
	 * @return 处理后的内容
	 * @param content
	 *            文章内容 <b>HTML文本</b>
	 */
	String handle(String content);

	/**
	 * 用来处理预览文章
	 * 
	 * @return 处理后的内容
	 * @param content
	 *            文章内容 <b>HTML文本</b>
	 */
	default String handlePreview(String content) {
		return handle(content);
	}
}
