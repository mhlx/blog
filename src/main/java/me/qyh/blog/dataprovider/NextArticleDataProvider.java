package me.qyh.blog.dataprovider;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.ArticleService;

@Component
public class NextArticleDataProvider extends PrevOrNextArticleDataProvider {

	public NextArticleDataProvider(ArticleService articleService) {
		super("nextArticle", articleService, false);
	}

}
