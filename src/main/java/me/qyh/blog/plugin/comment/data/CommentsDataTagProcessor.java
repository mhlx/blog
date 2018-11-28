package me.qyh.blog.plugin.comment.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;
import me.qyh.blog.plugin.comment.entity.CommentMode;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.plugin.comment.vo.CommentPageResult;
import me.qyh.blog.plugin.comment.vo.CommentQueryParam;
import me.qyh.blog.template.render.data.DataTagProcessor;

public class CommentsDataTagProcessor extends DataTagProcessor<CommentPageResult> {
	@Autowired
	private CommentService commentService;

	public CommentsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected CommentPageResult query(Attributes attributes) throws LogicException {
		CommentQueryParam param = new CommentQueryParam();

		String moduleTypeStr = attributes.getString("moduleType").orElse(null);
		String moduleIdStr = attributes.getString("moduleId").orElse(null);
		if (moduleIdStr != null && moduleTypeStr != null) {
			try {
				param.setModule(new CommentModule(moduleTypeStr, Integer.parseInt(moduleIdStr)));
			} catch (Exception e) {
				LOGGER.debug(e.getMessage(), e);
			}
		}

		param.setMode(attributes.getEnum("mode", CommentMode.class).orElse(CommentMode.LIST));
		param.setAsc(attributes.getBoolean("asc").orElse(true));
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(0));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));

		if (param.getCurrentPage() < 0) {
			param.setCurrentPage(0);
		}

		int pageSize = commentService.getCommentConfig().getPageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		param.setStatus(!Environment.hasAuthencated() ? CommentStatus.NORMAL : null);

		return commentService.queryComment(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("moduleType", "moduleId", "mode", "asc", "currentPage", "pageSize");
	}

}
