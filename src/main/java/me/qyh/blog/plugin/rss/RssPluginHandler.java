package me.qyh.blog.plugin.rss;

import me.qyh.blog.core.plugin.PluginHandlerSupport;

public class RssPluginHandler extends PluginHandlerSupport {

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.register(RssView.class.getName(), simpleBeanDefinition(RssView.class));
		registry.register(RssController.class.getName(), simpleBeanDefinition(RssController.class));
	}

}
