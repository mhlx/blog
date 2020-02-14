package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.vo.ArticleQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class ArticlesDataProvider extends DataProviderSupport<PageResult<Article>> {

	private final ArticleService articleService;

	public ArticlesDataProvider(ArticleService articleService) {
		super("articlePage");
		this.articleService = articleService;
	}

	@Override
	public PageResult<Article> provide(Map<String, String> attributesMap) throws Exception {
		return articleService.queryArticle(bindQueryParam(attributesMap));
	}

	private ArticleQueryParam bindQueryParam(Map<String, String> attributesMap) throws BindException {
		ArticleQueryParam param = bind(new ArticleQueryParam(), attributesMap);
		param.setStatus(ArticleStatus.PUBLISHED);
		param.setIgnoreLevel(false);
		param.setIgnorePaging(false);
		return param;
	}
}
