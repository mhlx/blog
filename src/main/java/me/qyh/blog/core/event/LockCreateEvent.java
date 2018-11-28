package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Lock;

public class LockCreateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Lock lock;

	public LockCreateEvent(Object source, Lock lock) {
		super(source);
		this.lock = lock;
	}

	public Lock getLock() {
		return lock;
	}

}
