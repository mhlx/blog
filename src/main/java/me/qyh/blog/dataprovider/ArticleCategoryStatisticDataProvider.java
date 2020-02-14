package me.qyh.blog.dataprovider;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.ArticleService;
import me.qyh.blog.vo.ArticleCategoryStatistic;
import me.qyh.blog.web.template.tag.DataProvider;

@Component
public class ArticleCategoryStatisticDataProvider extends DataProvider<List<ArticleCategoryStatistic>> {

	private final ArticleService articleService;

	public ArticleCategoryStatisticDataProvider(ArticleService articleService) {
		super("articleCategoryStatistics");
		this.articleService = articleService;
	}

	@Override
	public List<ArticleCategoryStatistic> provide(Map<String, String> attributesMap) {
		return articleService.getArticleCategoryStatistic();
	}
}
