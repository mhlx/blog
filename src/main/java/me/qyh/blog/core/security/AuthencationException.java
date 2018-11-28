package me.qyh.blog.core.security;

/**
 * 权限校验异常
 * 
 * @author Administrator
 *
 */
public class AuthencationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * default
	 */
	public AuthencationException() {
		this(null);
	}

	/**
	 * @param msg
	 *            error msg
	 */
	public AuthencationException(String msg) {
		super(msg, null, false, false);
	}

}
