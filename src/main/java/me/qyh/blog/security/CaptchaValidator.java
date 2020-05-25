package me.qyh.blog.security;

import me.qyh.blog.exception.BadRequestException;

public interface CaptchaValidator {

	/**
	 * 
	 * @param key
	 * @param code
	 * @throws BadRequestException
	 */
	void validate(String key, String code);

}
