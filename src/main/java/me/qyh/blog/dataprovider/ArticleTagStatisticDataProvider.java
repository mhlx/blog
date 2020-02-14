package me.qyh.blog.dataprovider;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.ArticleService;
import me.qyh.blog.vo.ArticleTagStatistic;
import me.qyh.blog.web.template.tag.DataProvider;

@Component
public class ArticleTagStatisticDataProvider extends DataProvider<List<ArticleTagStatistic>> {

	private final ArticleService articleService;

	public ArticleTagStatisticDataProvider(ArticleService articleService) {
		super("articleTagStatistics");
		this.articleService = articleService;
	}

	@Override
	public List<ArticleTagStatistic> provide(Map<String, String> attributesMap) throws Exception {
		String category = attributesMap.get("category");
		return articleService.getArticleTagStatistic(category);
	}
}
