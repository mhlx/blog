package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.ArticleNav;

public class ArticleNavDataTagProcessor extends DataTagProcessor<ArticleNav> {

	private static final String ID_OR_ALIAS = "idOrAlias";
	private static final String REF_ARTICLE = "article";

	@Autowired
	private ArticleService articleService;

	public ArticleNavDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected ArticleNav query(Attributes attributes) throws LogicException {
		boolean queryLock = attributes.getBoolean("queryLock").orElse(false);
		Object refArt = attributes.get(REF_ARTICLE).orElse(null);
		if (refArt != null) {
			return articleService.getArticleNav((Article) refArt, queryLock).orElse(null);
		}
		return attributes.getString(ID_OR_ALIAS)
				.flatMap(idOrAlias -> articleService.getArticleNav(idOrAlias, queryLock)).orElse(null);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("queryLock", "idOrAlias", "ref-article");
	}

}
