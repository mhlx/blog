package me.qyh.blog.security;

import me.qyh.blog.exception.BadRequestException;

public interface CaptchaValidator {

	void validate(String key, String code);

}
