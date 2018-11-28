package me.qyh.blog.web.security;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.exception.LogicException;

public interface CaptchaValidator {

	void doValidate(HttpServletRequest request) throws LogicException;

}
