package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class ArticleDataProvider extends DataProviderSupport<Article> {

	private final ArticleService articleService;

	public ArticleDataProvider(ArticleService articleService) {
		super("article");
		this.articleService = articleService;
	}

	@Override
	public Article provide(Map<String, String> attributesMap) throws Exception {
		String idOrAlias = attributesMap.get("idOrAlias");
		if (idOrAlias == null) {
			BindingResult br = createBindingResult(attributesMap);
			br.rejectValue("idOrAlias", "NotBlank", "文章ID或别名不能为空");
			throw new BindException(br);
		}
		return articleService.getArticle(idOrAlias).filter(a -> a.getStatus().equals(ArticleStatus.PUBLISHED))
				.orElse(null);
	}
}
