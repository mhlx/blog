package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Lock;

/**
 * 锁删除事件
 * 
 * @author mhlx
 *
 */
public final class LockDelEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Lock lock;

	public LockDelEvent(Object source, Lock lock) {
		super(source);
		this.lock = lock;
	}

	public Lock getLock() {
		return lock;
	}

}
