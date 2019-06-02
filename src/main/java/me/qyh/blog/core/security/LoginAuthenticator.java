package me.qyh.blog.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginAuthenticator {

	@Autowired(required = false)
	private GoogleAuthenticator authenticator;

	public boolean enable() {
		return authenticator != null;
	}

	public boolean checkCode(String str) {
		return authenticator.checkCode(str);
	}
}
