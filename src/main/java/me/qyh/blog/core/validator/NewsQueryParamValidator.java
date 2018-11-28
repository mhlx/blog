package me.qyh.blog.core.validator;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.vo.NewsQueryParam;

/**
 * 
 * @author Administrator
 *
 */
@Component
public class NewsQueryParamValidator implements Validator {
	private static final int MAX_CONTENT_LENGTH = 50;

	@Override
	public boolean supports(Class<?> clazz) {
		return NewsQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		NewsQueryParam param = (NewsQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		Date begin = param.getBegin();
		Date end = param.getEnd();
		if (begin != null && end != null && begin.after(end)) {
			param.setBegin(null);
			param.setEnd(null);
		}
		String content = param.getContent();
		if (content != null && content.length() > MAX_CONTENT_LENGTH) {
			param.setContent(content.substring(0, MAX_CONTENT_LENGTH));
		}
	}

}
