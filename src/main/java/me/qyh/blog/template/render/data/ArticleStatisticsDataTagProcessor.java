package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.ArticleStatistics;

public class ArticleStatisticsDataTagProcessor extends DataTagProcessor<ArticleStatistics> {

	@Autowired
	private ArticleService articleService;

	public ArticleStatisticsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected ArticleStatistics query(Attributes attributes) throws LogicException {
		return articleService.queryArticleStatistics();
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}

}
