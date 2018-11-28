package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.vo.NewsStatistics;

public class NewsStatisticsDataTagProcessor extends DataTagProcessor<NewsStatistics> {

	@Autowired
	private NewsService newsService;

	public NewsStatisticsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected NewsStatistics query(Attributes attributes) throws LogicException {
		return newsService.queryNewsStatistics();
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}
}
