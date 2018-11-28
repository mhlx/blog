package me.qyh.blog.template.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.vo.FragmentQueryParam;

@Component
public class FragmentQueryParamValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 5;

	@Override
	public boolean supports(Class<?> clazz) {
		return FragmentQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		FragmentQueryParam param = (FragmentQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String name = param.getName();
		if (!Validators.isEmptyOrNull(name, true) && param.getName().length() > MAX_NAME_LENGTH) {
			param.setName(param.getName().substring(0, MAX_NAME_LENGTH));
		}
	}
}
