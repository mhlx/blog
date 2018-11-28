package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.TagService;
import me.qyh.blog.core.vo.TagStatistics;

public class TagStatisticsDataTagProcessor extends DataTagProcessor<TagStatistics> {

	@Autowired
	private TagService tagService;

	public TagStatisticsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected TagStatistics query(Attributes attributes) throws LogicException {
		return tagService.queryTagStatistics();
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}

}
