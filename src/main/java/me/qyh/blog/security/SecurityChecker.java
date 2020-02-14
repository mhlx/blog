package me.qyh.blog.security;

import java.util.Map;
import java.util.Objects;

import me.qyh.blog.BlogContext;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.PasswordProtectException;

public class SecurityChecker {

	private SecurityChecker() {
		super();
	}

	public static boolean locked(PasswordProtect p) {
		if (BlogContext.isAuthenticated()) {
			return false;
		}
		String password = p.getPassword();
		if (password != null) {
			String id = p.getResId();
			Map<String, String> pwdMap = BlogContext.getPasswordMap();
			if (!Objects.equals(password, pwdMap.get(id))) {
				return true;
			}
		}
		return false;
	}

	public static void check(Object object) {
		if (BlogContext.isAuthenticated()) {
			return;
		}
		if (object instanceof PrivateProtect) {
			PrivateProtect p = (PrivateProtect) object;
			if (p.getIsPrivate() != null && p.getIsPrivate()) {
				throw new AuthenticationException();
			}
		}
		if (object instanceof PasswordProtect) {
			PasswordProtect p = (PasswordProtect) object;
			String password = p.getPassword();
			if (password != null) {
				String id = p.getResId();
				Map<String, String> pwdMap = BlogContext.getPasswordMap();
				if (!Objects.equals(password, pwdMap.get(id))) {
					throw new PasswordProtectException(id, !pwdMap.containsKey(id));
				}
			}
		}
	}
}
