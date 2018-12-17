package me.qyh.blog.plugin.comment.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.plugin.comment.service.CommentConfig;

@Component
public class CommentConfigValidator implements Validator {

	private static final int[] LIMIT_SECOND_RANGE = { 1, 300 };
	private static final int[] LIMIT_COUNT_RANGE = { 1, 100 };
	private static final int[] PAGE_SIZE_RANGE = { 1, 50 };

	@Override
	public boolean supports(Class<?> clazz) {
		return CommentConfig.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		CommentConfig config = (CommentConfig) target;
		if (config.getEditor() == null) {
			errors.reject("commentConfig.editor.blank", "评论编辑器不能为空");
			return;
		}
		if (config.getCheck() == null) {
			errors.reject("commentConfig.check.blank", "评论审核不能为空");
			return;
		}
		Integer limitCount = config.getLimitCount();
		if (limitCount == null) {
			errors.reject("commentConfig.limitCount.blank", "限制评论数不能为空");
			return;
		}
		if (limitCount < LIMIT_COUNT_RANGE[0] || limitCount > LIMIT_COUNT_RANGE[1]) {
			errors.reject("commentConfig.limitCount.invalid",
					new Object[] { LIMIT_COUNT_RANGE[0], LIMIT_COUNT_RANGE[1] },
					"限制评论数应该在" + LIMIT_COUNT_RANGE[0] + "和" + LIMIT_COUNT_RANGE[1] + "之间");
			return;
		}

		Integer limitSec = config.getLimitSec();
		if (limitSec == null) {
			errors.reject("commentConfig.limitSec.blank", "限制评论时间不能为空");
			return;
		}
		if (limitSec < LIMIT_SECOND_RANGE[0] || limitSec > LIMIT_SECOND_RANGE[1]) {
			errors.reject("commentConfig.limitSec.invalid",
					new Object[] { LIMIT_SECOND_RANGE[0], LIMIT_SECOND_RANGE[1] },
					"限制评论时间应该在" + LIMIT_SECOND_RANGE[0] + "和" + LIMIT_SECOND_RANGE[1] + "之间");
			return;
		}

		int pageSize = config.getPageSize();
		if (pageSize < PAGE_SIZE_RANGE[0] || pageSize > PAGE_SIZE_RANGE[1]) {
			errors.reject("commentConfig.pagesize.invalid", new Object[] { PAGE_SIZE_RANGE[0], PAGE_SIZE_RANGE[1] },
					"评论分页应该在" + PAGE_SIZE_RANGE[0] + "和" + PAGE_SIZE_RANGE[1] + "之间");
			return;
		}

		String finalName = CommentValidator.validateName(config.getNickname(), errors);
		if (errors.hasErrors()) {
			return;
		}
		config.setNickname(finalName);
	}

}
