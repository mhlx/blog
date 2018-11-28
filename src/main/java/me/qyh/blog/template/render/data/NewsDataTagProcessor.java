package me.qyh.blog.template.render.data;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.service.NewsService;

public class NewsDataTagProcessor extends DataTagProcessor<News> {

	@Autowired
	private NewsService newsService;

	public NewsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected News query(Attributes attributes) throws LogicException {
		Integer id = attributes.getInteger("id").orElse(null);
		boolean ignoreException = attributes.getBoolean("ignoreException").orElse(false);
		if (id == null) {
			if (ignoreException) {
				return null;
			}
			throw new ResourceNotFoundException("news.notExists", "动态不存在");
		}
		Optional<News> op = newsService.getNews(id);
		if (op.isPresent()) {
			return op.get();
		}
		if (!ignoreException) {
			throw new ResourceNotFoundException("news.notExists", "动态不存在");
		}
		return null;
	}

	@Override
	public List<String> getAttributes() {
		return List.of("id", "ignoreException");
	}

}
