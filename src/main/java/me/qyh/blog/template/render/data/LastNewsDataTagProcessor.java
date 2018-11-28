package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;

public class LastNewsDataTagProcessor extends DataTagProcessor<List<News>> {

	@Autowired
	private NewsService newsService;

	private static final int DEFAULT_LIMIT = 5;
	private static final int MAX_LIMIT = 20;

	public LastNewsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<News> query(Attributes attributes) throws LogicException {
		return newsService.queryLastNews(getLimit(attributes),
				attributes.getString("queryLock").map(Boolean::parseBoolean).orElse(false));
	}

	private int getLimit(Attributes attributes) {
		return attributes.getInteger("limit").filter(limit -> limit > 0 && limit <= MAX_LIMIT).orElse(DEFAULT_LIMIT);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("limit", "queryLock");
	}
}
