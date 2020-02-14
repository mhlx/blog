package me.qyh.blog.dataprovider;

import java.util.List;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import me.qyh.blog.entity.Comment;
import me.qyh.blog.service.CommentService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class LastCommentsDataProvider extends DataProviderSupport<List<Comment>> {

	private final CommentService commentService;

	public LastCommentsDataProvider(CommentService commentService) {
		super("lastComments");
		this.commentService = commentService;
	}

	@Override
	public List<Comment> provide(Map<String, String> attributesMap) throws Exception {
		int num;
		String numStr = attributesMap.get("num");
		if (numStr == null) {
			num = 5;
		} else {
			try {
				num = Integer.parseInt(numStr);
			} catch (NumberFormatException e) {
				throw new TypeMismatchException(numStr, Integer.class);
			}

			if (num < 1) {
				BindingResult br = createBindingResult(attributesMap);
				br.rejectValue("num", "Positive", "最近评论数量不能小于1");
				throw new BindException(br);
			}
		}
		boolean queryAdmin = Boolean.parseBoolean(attributesMap.get("queryAdmin"));
		return commentService.getLastComments(num, queryAdmin);
	}
}
