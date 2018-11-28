package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.CommentServer;

public class CommentCountDataTagProcessor extends DataTagProcessor<Integer> {

	@Autowired(required = false)
	private CommentServer commentServer;

	public CommentCountDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected Integer query(Attributes attributes) throws LogicException {
		if (commentServer == null) {
			return 0;
		}
		String moduleType = attributes.getString("moduleType").orElse(null);
		String moduleId = attributes.getString("moduleId").orElse(null);
		if (moduleType != null && moduleId != null) {
			try {
				return commentServer.queryCommentNum(moduleType, Integer.parseInt(moduleId)).orElse(0);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 0;
	}

	@Override
	public List<String> getAttributes() {
		return List.of("moduleType", "moduleId");
	}

}
