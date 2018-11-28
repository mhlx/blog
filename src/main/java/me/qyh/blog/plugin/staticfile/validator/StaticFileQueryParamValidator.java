package me.qyh.blog.plugin.staticfile.validator;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.plugin.staticfile.vo.StaticFileQueryParam;

@Component
public class StaticFileQueryParamValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_EXTENSION_LENGTH = 5;

	@Override
	public boolean supports(Class<?> clazz) {
		return StaticFileQueryParam.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		StaticFileQueryParam param = (StaticFileQueryParam) target;
		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}
		String name = param.getName();
		if (name != null && name.length() > MAX_NAME_LENGTH) {
			param.setName(name.substring(0, MAX_NAME_LENGTH));
		}
		// 最多只允许 5 个后缀
		param.setExtensions(param.getExtensions().stream().limit(MAX_EXTENSION_LENGTH).collect(Collectors.toSet()));
	}

}
