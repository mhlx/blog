package me.qyh.blog.core.plugin;

import me.qyh.blog.core.service.ArticleHitHandler;

public interface ArticleHitHandlerRegistry {

	ArticleHitHandlerRegistry register(ArticleHitHandler handler);

}
