package me.qyh.blog.plugin.syslock.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.annotations.Expose;

import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.BCrypts;
import me.qyh.blog.core.util.Validators;

/**
 * 密码锁
 * 
 * @author Administrator
 *
 */
public class PasswordLock extends SysLock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String PASSWORD_PARAMETER = "password";

	@Expose(serialize = false)
	private String password;

	/**
	 * default
	 */
	public PasswordLock() {
		super(SysLockType.PASSWORD);
	}

	@Override
	public LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException {
		final String requestPassword = request.getParameter(PASSWORD_PARAMETER);
		if (Validators.isEmptyOrNull(requestPassword, true)) {
			throw new LogicException(new Message("lock.password.password.blank", "密码不能为空"));
		}
		return new LockKey() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String lockId() {
				return getId();
			}

			@Override
			public Serializable getKey() {
				return requestPassword;
			}
		};
	}

	@Override
	public void tryOpen(LockKey key) throws LogicException {
		Objects.requireNonNull(key);
		Object keyData = key.getKey();
		if (BCrypts.matches(keyData.toString(), password)) {
			return;
		}
		throw new LogicException(new Message("lock.password.unlock.fail", "密码验证失败"));
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
