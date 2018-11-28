package me.qyh.blog.core.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.util.Validators;

@Component
public class UserValidator implements Validator {

	public static final int MAX_NAME_LENGTH = 10;// 用户名最大长度为10位
	public static final int MAX_PWD_LENGTH = 16; // 密码最大长度为16位
	public static final int MAX_EMAIL_LENGTH = 100;// 邮箱最大长度为100位
	public static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User) target;
		String name = user.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("user.name.blank", "用户名为空");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("user.name.toolong", new Object[] { MAX_NAME_LENGTH }, "用户名不能超过" + MAX_NAME_LENGTH + "位");
			return;
		}
		String password = user.getPassword();
		if (password != null && !password.isEmpty() && password.length() > MAX_PWD_LENGTH) {
			errors.reject("user.password.toolong", new Object[] { MAX_PWD_LENGTH }, "密码不能超过" + MAX_PWD_LENGTH + "位");
			return;
		}
		String email = user.getEmail();
		if (email != null) {
			email = email.strip();
			if (!email.isEmpty()) {
				if (email.length() > MAX_EMAIL_LENGTH) {
					errors.reject("user.email.toolong", new Object[] { MAX_EMAIL_LENGTH },
							"邮箱不能超过" + MAX_EMAIL_LENGTH + "位");
					return;
				}
				if (!EMAIL_PATTERN.matcher(email).matches()) {
					errors.reject("user.email.invalid", "邮箱不是正确的格式");
					return;
				}
				user.setEmail(email);
			}
		}
	}

}
