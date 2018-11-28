package me.qyh.blog.plugin.syslock.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.syslock.entity.PasswordLock;

@Component
public class PasswordLockValidator extends SysLockValidator {

	private static final int MAX_PASSWORD_LENGTH = 16;

	@Override
	public boolean supports(Class<?> clazz) {
		return PasswordLock.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		super.validate(target, errors);
		if (errors.hasErrors()) {
			return;
		}
		PasswordLock lock = (PasswordLock) target;
		String password = lock.getPassword();
		if (Validators.isEmptyOrNull(password, true)) {
			errors.reject("lock.pwd.empty", "锁的密码不能为空");
			return;
		}
		if (password.length() > MAX_PASSWORD_LENGTH) {
			errors.reject("lock.pwd.toolong", "锁的密码不能超过" + MAX_PASSWORD_LENGTH + "个字符");
		}
	}
}
