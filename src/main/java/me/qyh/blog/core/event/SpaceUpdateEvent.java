package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Space;

/**
 * 空间更新事件
 * 
 */
public final class SpaceUpdateEvent extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Space oldSpace;
	private final Space newSpace;

	public SpaceUpdateEvent(Object source, Space oldSpace, Space newSpace) {
		super(source);
		this.oldSpace = oldSpace;
		this.newSpace = newSpace;
	}

	public Space getOldSpace() {
		return oldSpace;
	}

	public Space getNewSpace() {
		return newSpace;
	}

}
