package me.qyh.blog.exception;

import me.qyh.blog.Message;

public class LoginFailException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Message error;

	public LoginFailException(Message error) {
		super(null, null, false, false);
		this.error = error;
	}

	public LoginFailException(String code, String defaultMessage, Object... args) {
		this(new Message(code, defaultMessage, args));
	}

	public Message getError() {
		return error;
	}
}
