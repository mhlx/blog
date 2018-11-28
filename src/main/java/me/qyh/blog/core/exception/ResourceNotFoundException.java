package me.qyh.blog.core.exception;

import me.qyh.blog.core.message.Message;

/**
 * 
 * @author Administrator
 *
 */
public class ResourceNotFoundException extends LogicException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(Message message) {
		super(message);
	}

	public ResourceNotFoundException(String code, Object... args) {
		super(code, args);
	}

	public ResourceNotFoundException(String code, String defaultMessage, Object... args) {
		super(code, defaultMessage, args);
	}

}
