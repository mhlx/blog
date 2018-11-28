package me.qyh.blog.core.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.LoginBean;

@Component
public class LoginBeanValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return LoginBean.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		LoginBean loginBean = (LoginBean) target;
		String username = loginBean.getUsername();
		if (Validators.isEmptyOrNull(username, true)) {
			errors.reject("login.username.blank", "用户名不能为空");
			return;
		}
		if (username.length() > UserValidator.MAX_NAME_LENGTH) {
			errors.reject("login.username.valid", "无效的用户名");
			return;
		}
		String password = loginBean.getPassword();
		if (Validators.isEmptyOrNull(password, true)) {
			errors.reject("login.password.blank", "密码不能为空");
			return;
		}
		if (password.length() > UserValidator.MAX_PWD_LENGTH) {
			errors.reject("login.password.valid", "无效的密码");
		}
	}

}
