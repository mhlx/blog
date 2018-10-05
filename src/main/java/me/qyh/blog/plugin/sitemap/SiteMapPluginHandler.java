package me.qyh.blog.plugin.sitemap;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class SiteMapPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pluginProperties = PluginProperties.getInstance();
	private final boolean enable = pluginProperties.get("plugin.sitemap.enable").map(Boolean::parseBoolean)
			.orElse(true);

	@Override
	protected void registerBean(BeanRegistry registry) {
		registry.scanAndRegister("component");
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.register(SiteMapController.class.getName(), simpleBeanDefinition(SiteMapController.class));
	}

	@Override
	public boolean enable() {
		return enable;
	}

}
