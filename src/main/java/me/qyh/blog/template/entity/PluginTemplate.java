package me.qyh.blog.template.entity;

import me.qyh.blog.core.entity.BaseEntity;
import me.qyh.blog.template.Template;

public class PluginTemplate extends BaseEntity implements Template {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String PLUGIN_TEMPLATE_PREFIX = TEMPLATE_PREFIX + "Plugin" + SPLITER;

	private String pluginName;
	private String template;
	private String name;

	public PluginTemplate() {
		super();
	}

	public PluginTemplate(String pluginName, String template, String name) {
		super();
		this.pluginName = pluginName;
		this.template = template;
		this.name = name;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public String getTemplateName() {
		return PLUGIN_TEMPLATE_PREFIX + pluginName + SPLITER + name;
	}

	public String getName() {
		return name;
	}

	@Override
	public Template cloneTemplate() {
		return new PluginTemplate(pluginName, template, name);
	}

	@Override
	public final boolean isCallable() {
		return false;
	}

	@Override
	public final boolean equalsTo(Template other) {
		return false;
	}

	@Override
	public final boolean cacheable() {
		return false;
	}

	public static boolean isPluginTemplate(String templateName) {
		return templateName != null && templateName.startsWith(PLUGIN_TEMPLATE_PREFIX);
	}
}
