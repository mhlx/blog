package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.validation.BindException;

import me.qyh.blog.entity.Comment;
import me.qyh.blog.service.CommentService;
import me.qyh.blog.vo.CommentQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.tag.DataProviderSupport;

public class CommentsDataProvider extends DataProviderSupport<PageResult<Comment>> {

	private final CommentService commentService;

	public CommentsDataProvider(CommentService commentService) {
		super("commentPage");
		this.commentService = commentService;
	}

	@Override
	public PageResult<Comment> provide(Map<String, String> attributesMap) throws Exception {
		return commentService.queryComments(bindQueryParam(attributesMap));
	}

	private CommentQueryParam bindQueryParam(Map<String, String> attributesMap) throws BindException {
		return bind(new CommentQueryParam(), attributesMap);
	}
}
