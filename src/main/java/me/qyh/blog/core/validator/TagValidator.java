package me.qyh.blog.core.validator;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.util.Validators;

@Component
public class TagValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;

	@Override
	public boolean supports(Class<?> clazz) {
		return Tag.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Tag tag = (Tag) target;
		String name = tag.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("tag.name.blank", "文章标签名不能为空");
			return;
		}
		name = Jsoup.clean(name, Whitelist.none()).strip();
		if (name.isEmpty()) {
			errors.reject("tag.name.blank", "文章标签名不能为空");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("tag.name.toolong", new Object[] { MAX_NAME_LENGTH }, "文章标签名不能超过" + MAX_NAME_LENGTH + "个字符");
			return;
		}
		for (char ch : name.toCharArray()) {
			if (!Character.isLetterOrDigit(ch)) {
				errors.reject("tag.name.invalid", new Object[] { name }, "标签名:" + name + "异常");
				return;
			}
		}
		tag.setName(name);
	}
}
