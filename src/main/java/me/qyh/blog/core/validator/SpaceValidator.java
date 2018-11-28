package me.qyh.blog.core.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.util.Validators;

@Component
public class SpaceValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;
	public static final int MAX_ALIAS_LENGTH = 20;

	@Override
	public boolean supports(Class<?> clazz) {
		return Space.class.equals(clazz);
	}

	public static boolean isValidAlias(String alias) {
		return alias != null && alias.length() < MAX_ALIAS_LENGTH && Validators.isLetterOrNum(alias);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Space space = (Space) target;
		String name = space.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("space.name.blank", "空间名为空");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("space.name.toolong", new Object[] { MAX_NAME_LENGTH }, "空间名不能超过" + MAX_NAME_LENGTH + "个字符");
			return;
		}

		if (!space.hasId()) {
			String alias = space.getAlias();
			if (Validators.isEmptyOrNull(alias, true)) {
				errors.reject("space.alias.blank", "别名为空");
				return;
			}
			if (alias.length() > MAX_ALIAS_LENGTH) {
				errors.reject("space.alias.toolong", new Object[] { MAX_ALIAS_LENGTH },
						"别名不能超过" + MAX_NAME_LENGTH + "个字符");
				return;
			}
			if (!Validators.isLetterOrNum(alias)) {
				errors.reject("space.alias.invalid", "别名只能包含英文字母和数字");
				return;
			}
		} else {
			space.setAlias(null);
		}

		if (space.getIsPrivate() == null) {
			errors.reject("space.private.blank", "空间私有性不能为空");
			return;
		}

		if (space.getIsDefault() == null) {
			errors.reject("space.isDefault.blank", "是否是默认空间不能为空");
		}
	}
}
