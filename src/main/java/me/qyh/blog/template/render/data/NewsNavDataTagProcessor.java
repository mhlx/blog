package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.vo.NewsNav;

public class NewsNavDataTagProcessor extends DataTagProcessor<NewsNav> {

	private static final String ID = "id";
	private static final String REF_NEWS = "news";

	@Autowired
	private NewsService newsService;

	public NewsNavDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected NewsNav query(Attributes attributes) throws LogicException {
		boolean queryLock = attributes.getBoolean("queryLock").orElse(false);
		Object v = attributes.get(REF_NEWS).orElse(null);
		if (v != null) {
			return newsService.getNewsNav((News) v, queryLock).orElse(null);
		}
		return attributes.getInteger(ID).flatMap(id -> newsService.getNewsNav(id, queryLock)).orElse(null);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("id", "ref-news");
	}
}
