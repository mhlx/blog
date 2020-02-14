package me.qyh.blog.dataprovider;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.ArticleService;

@Component
public class PrevArticleDataProvider extends PrevOrNextArticleDataProvider {

	public PrevArticleDataProvider(ArticleService articleService) {
		super("prevArticle", articleService, true);
	}

}
