package me.qyh.blog.core.exception;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.message.Message;

/**
 * 锁异常
 * 
 * @author Administrator
 *
 */
public class LockException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Lock lock;
	private final Message error;

	/**
	 * @param lock
	 *            锁
	 * @param error
	 *            错误信息
	 */
	public LockException(Lock lock, Message error) {
		super(null, null, false, false);
		this.lock = lock;
		this.error = error;
	}

	public Lock getLock() {
		return lock;
	}

	public Message getError() {
		return error;
	}
}
