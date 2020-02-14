package me.qyh.blog.file;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import me.qyh.blog.utils.FileUtils;

public class PathValidator implements ConstraintValidator<Path, String> {

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		if (object == null) {
			return true;
		}
		return FileUtils.validPath(object);
	}
}