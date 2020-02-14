package me.qyh.blog.dataprovider;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import me.qyh.blog.entity.Article;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

class PrevOrNextArticleDataProvider extends DataProviderSupport<Article> {
	private final ArticleService articleService;
	private final boolean prev;

	public PrevOrNextArticleDataProvider(String name, ArticleService articleService, boolean prev) {
		super(name);
		this.articleService = articleService;
		this.prev = prev;
	}

	@Override
	public Article provide(Map<String, String> attributesMap) throws Exception {
		String idOrAlias = attributesMap.get("idOrAlias");
		if (idOrAlias == null) {
			BindingResult br = createBindingResult(attributesMap);
			br.rejectValue("idOrAlias", "NotBlank", "文章id或别名不能为空");
			throw new BindException(br);
		}
		String categories = attributesMap.get("categories");
		String tags = attributesMap.get("tags");
		Set<String> categorySet = categories == null ? null
				: Arrays.stream(categories.split(",")).collect(Collectors.toSet());
		Set<String> tagSet = tags == null ? null : Arrays.stream(tags.split(",")).collect(Collectors.toSet());

		if (prev) {
			return articleService.prev(idOrAlias, categorySet, tagSet).orElse(null);
		}

		return articleService.next(idOrAlias, categorySet, tagSet).orElse(null);
	}
}
