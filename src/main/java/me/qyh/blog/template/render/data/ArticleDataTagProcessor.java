package me.qyh.blog.template.render.data;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.service.ArticleService;

/**
 * 文章详情数据数据器
 * 
 * @author Administrator
 *
 */
public class ArticleDataTagProcessor extends DataTagProcessor<Article> {

	@Autowired
	private ArticleService articleService;

	public ArticleDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected Article query(Attributes attributes) throws LogicException {
		// 首先从属性中获取
		String idOrAlias = attributes.getString("idOrAlias").orElse(null);
		boolean ignoreException = attributes.getBoolean("ignoreException").orElse(false);
		if (idOrAlias == null) {
			if (ignoreException) {
				return null;
			}
			throw new ResourceNotFoundException("article.notExists", "文章不存在");
		}
		Optional<Article> op = articleService.getArticleForView(idOrAlias);
		if (op.isPresent()) {
			return op.get();
		}
		if (!ignoreException) {
			throw new ResourceNotFoundException("article.notExists", "文章不存在");
		}
		return null;
	}

	@Override
	public List<String> getAttributes() {
		return List.of("idOrAlias", "ignoreException");
	}
}
