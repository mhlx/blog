package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.vo.CommentStatistics;

public class CommentStatisticsDataTagProcessor extends DataTagProcessor<CommentStatistics> {

	@Autowired(required = false)
	private CommentServer commentServer;

	public CommentStatisticsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected CommentStatistics query(Attributes attributes) throws LogicException {
		if (commentServer == null) {
			return new CommentStatistics();
		}
		return commentServer.queryCommentStatistics(Environment.getSpace());
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}

}
