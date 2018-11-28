package me.qyh.blog.file.store.local;

public class ProcessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessException(String message) {
		super(message);
	}

}
