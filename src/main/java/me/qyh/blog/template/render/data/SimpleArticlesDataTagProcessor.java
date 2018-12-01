package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.util.Validators;

public class SimpleArticlesDataTagProcessor extends DataTagProcessor<List<Article>> {

	@Autowired
	private ArticleService articleService;

	private static final int DEFAULT_LIMIT = 5;
	private static final int MAX_LIMIT = 20;

	public SimpleArticlesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<Article> query(Attributes attributes) throws LogicException {
		String content = attributes.getString("query").orElse(null);
		if (Validators.isEmptyOrNull(content, true)) {
			return List.of();
		}
		int max = attributes.getInteger("max").orElse(DEFAULT_LIMIT);
		if (max > MAX_LIMIT) {
			max = MAX_LIMIT;
		}
		return articleService.querySimpleArticles(content, max);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("query", "max");
	}
}
