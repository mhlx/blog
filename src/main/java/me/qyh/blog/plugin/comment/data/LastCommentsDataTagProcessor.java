package me.qyh.blog.plugin.comment.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.template.render.data.DataTagProcessor;

public class LastCommentsDataTagProcessor extends DataTagProcessor<List<Comment>> {

	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 50;

	@Autowired
	private CommentService commentService;

	public LastCommentsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<Comment> query(Attributes attributes) throws LogicException {
		return attributes.getString("moduleType").map(type -> commentService.queryLastComments(type,
				getLimit(attributes), attributes.getBoolean("queryAdmin").orElse(false))).orElse(new ArrayList<>());
	}

	private int getLimit(Attributes attributes) {
		return attributes.getInteger("limit").filter(limit -> limit > 0 && limit <= MAX_LIMIT).orElse(DEFAULT_LIMIT);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("moduleType", "queryAdmin", "limit");
	}

}
