package me.qyh.blog.template.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.util.Validators;

/**
 * 用来监听template缓存清除事件
 * 
 * <p>
 * <b>这个事件仅应该在事务完成后发布</b>
 * </p>
 * 
 * @author Administrator
 *
 */
public class TemplateEvitEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String[] templateNames;

	public TemplateEvitEvent(Object source, String... templateNames) {
		super(source);
		this.templateNames = templateNames;
	}

	public String[] getTemplateNames() {
		return templateNames;
	}

	public boolean clear() {
		return Validators.isEmpty(templateNames);
	}

}
