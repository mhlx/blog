package me.qyh.blog.core.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.vo.CommentStatistics;

public class EmptyCommentServer implements CommentServer {

	private EmptyCommentServer() {
		super();
	}

	public static final EmptyCommentServer INSTANCE = new EmptyCommentServer();

	@Override
	public Map<Integer, Integer> queryCommentNums(String module, Collection<Integer> moduleIds) {
		return new HashMap<>();
	}

	@Override
	public OptionalInt queryCommentNum(String module, Space space, boolean queryPrivate) {
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt queryCommentNum(String module, Integer moduleId) {
		return OptionalInt.empty();
	}

	@Override
	public CommentStatistics queryCommentStatistics(Space space) {
		return new CommentStatistics();
	}

	@Override
	public void deleteComments(String module, Integer moduleId) {

	}

}
