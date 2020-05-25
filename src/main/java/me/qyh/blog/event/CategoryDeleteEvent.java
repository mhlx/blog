package me.qyh.blog.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.entity.Category;

public class CategoryDeleteEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Category category;

	public CategoryDeleteEvent(Object source, Category category) {
		super(source);
		this.category = category;
	}

	public Category getCategory() {
		return category;
	}

}
