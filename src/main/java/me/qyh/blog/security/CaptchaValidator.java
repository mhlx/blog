package me.qyh.blog.security;

import javax.servlet.http.HttpServletRequest;

public interface CaptchaValidator {

	void validate(HttpServletRequest request);

}
