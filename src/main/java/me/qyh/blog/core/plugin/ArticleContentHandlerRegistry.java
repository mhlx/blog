package me.qyh.blog.core.plugin;

import me.qyh.blog.core.service.ArticleContentHandler;

public interface ArticleContentHandlerRegistry {

	ArticleContentHandlerRegistry register(ArticleContentHandler handler);

}
