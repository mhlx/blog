package me.qyh.blog.core.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Space;

/**
 * 空间删除事件
 * 
 * @author mhlx
 *
 */
public final class SpaceDelEvent extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Space space;// 被删除的空间

	public SpaceDelEvent(Object source, Space space) {
		super(source);
		this.space = space;
	}

	public Space getSpace() {
		return space;
	}

}
