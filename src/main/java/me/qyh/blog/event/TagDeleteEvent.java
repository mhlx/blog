package me.qyh.blog.event;

import me.qyh.blog.entity.Tag;
import org.springframework.context.ApplicationEvent;

public class TagDeleteEvent extends ApplicationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Tag tag;

    public TagDeleteEvent(Object source, Tag tag) {
        super(source);
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

}
