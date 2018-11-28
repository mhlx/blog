package me.qyh.blog.core.exception;

/**
 * 系统异常
 * <p>
 * 同时用来将checked exception转化为unchecked exception
 * </p>
 * 
 * @author Administrator
 *
 */
public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            异常信息
	 * @param cause
	 *            导致系统异常的异常
	 */
	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param message
	 *            异常信息
	 */
	public SystemException(String message) {
		super(message);
	}

}
