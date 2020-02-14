package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import me.qyh.blog.service.ArticleService;
import me.qyh.blog.vo.ArticleArchive;
import me.qyh.blog.vo.ArticleArchiveQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class ArticleArchivesDataProvider extends DataProviderSupport<PageResult<ArticleArchive>> {

	private final ArticleService articleService;

	public ArticleArchivesDataProvider(ArticleService articleService) {
		super("articleArchivePage");
		this.articleService = articleService;
	}

	@Override
	public PageResult<ArticleArchive> provide(Map<String, String> attributesMap) throws Exception {
		return articleService.queryArticleArchives(bindQueryParam(attributesMap));
	}

	private ArticleArchiveQueryParam bindQueryParam(Map<String, String> attributesMap) throws BindException {
		ArticleArchiveQueryParam param = bind(new ArticleArchiveQueryParam(), attributesMap);
		return param;
	}
}
