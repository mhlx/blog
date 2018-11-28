package me.qyh.blog.template.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.vo.TemplatePageQueryParam;

@Component
public class TemplatePageQueryParamValidator implements Validator {

	private static final int MAX_QUERY_LENGTH = 255;

	@Override
	public boolean supports(Class<?> clazz) {
		return TemplatePageQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TemplatePageQueryParam param = (TemplatePageQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String query = param.getQuery();
		if (!Validators.isEmptyOrNull(query, true) && param.getQuery().length() > MAX_QUERY_LENGTH) {
			param.setQuery(param.getQuery().substring(0, MAX_QUERY_LENGTH));
		}
	}
}
