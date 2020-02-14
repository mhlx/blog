package me.qyh.blog.exception;

public class InvalidCaptchaException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCaptchaException() {
		super(null, null, false, false);
	}

}
