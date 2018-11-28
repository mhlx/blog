package me.qyh.blog.core.exception;

import me.qyh.blog.core.message.Message;

/**
 * 业务异常，这个异常不做任何的日志
 * 
 * @author Administrator
 *
 */
public class RuntimeLogicException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final LogicException logicException;

	/**
	 * @param logicException
	 *            原始异常
	 */
	public RuntimeLogicException(LogicException logicException) {
		super(null, null, false, false);
		this.logicException = logicException;
	}

	/**
	 * 异常信息
	 * @param message
	 */
	public RuntimeLogicException(Message message) {
		this(new LogicException(message));
	}

	public LogicException getLogicException() {
		return logicException;
	}
}
