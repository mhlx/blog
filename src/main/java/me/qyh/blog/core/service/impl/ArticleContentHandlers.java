package me.qyh.blog.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.plugin.ArticleContentHandlerRegistry;
import me.qyh.blog.core.service.ArticleContentHandler;

/**
 * 用于支持多个ArticleContentHandler
 * 
 * @author Administrator
 *
 */
@Component
public class ArticleContentHandlers implements ArticleContentHandler, ArticleContentHandlerRegistry {

	private final List<ArticleContentHandler> handlers = new ArrayList<>();

	@Override
	public String handle(String content) {
		String handled = content;
		if (!CollectionUtils.isEmpty(handlers)) {
			for (ArticleContentHandler handler : handlers) {
				handled = Objects.requireNonNull(handler.handle(handled));
			}
		}
		return handled;
	}

	@Override
	public ArticleContentHandlerRegistry register(ArticleContentHandler handler) {
		this.handlers.add(handler);
		return this;
	}

}
