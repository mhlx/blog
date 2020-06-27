package me.qyh.blog.event;

import me.qyh.blog.entity.Category;
import org.springframework.context.ApplicationEvent;

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
