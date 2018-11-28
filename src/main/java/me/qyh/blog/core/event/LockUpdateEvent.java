package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Lock;

public class LockUpdateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Lock oldLock;
	private final Lock newLock;

	public LockUpdateEvent(Object source, Lock oldLock, Lock newLock) {
		super(source);
		this.oldLock = oldLock;
		this.newLock = newLock;
	}

	public Lock getOldLock() {
		return oldLock;
	}

	public Lock getNewLock() {
		return newLock;
	}

}
