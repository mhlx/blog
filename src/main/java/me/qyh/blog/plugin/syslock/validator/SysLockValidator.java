package me.qyh.blog.plugin.syslock.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.syslock.entity.SysLock;

public abstract class SysLockValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 20;

	@Override
	public void validate(Object target, Errors errors) {
		SysLock lock = (SysLock) target;
		String name = lock.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("lock.name.empty", "锁的名称不能为空");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("lock.name.toolong", "锁的名称不能超过" + MAX_NAME_LENGTH + "个字符");
			return;
		}
	}
}
