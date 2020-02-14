package me.qyh.blog.exception;

import me.qyh.blog.Message;

public class BadRequestException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Message error;

	public BadRequestException(String code, String defaultMessage, Object... args) {
		super(null, null, false, false);
		this.error = new Message(code, defaultMessage, args);
	}

	public BadRequestException(Message error) {
		super();
		this.error = error;
	}

	public Message getError() {
		return error;
	}
}
