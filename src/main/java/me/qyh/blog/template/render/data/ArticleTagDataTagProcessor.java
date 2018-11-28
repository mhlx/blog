package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.TagCount;

public class ArticleTagDataTagProcessor extends DataTagProcessor<List<TagCount>> {

	@Autowired
	private ArticleService articleService;

	public ArticleTagDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<TagCount> query(Attributes attributes) throws LogicException {
		return articleService.queryTags();
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}
}
