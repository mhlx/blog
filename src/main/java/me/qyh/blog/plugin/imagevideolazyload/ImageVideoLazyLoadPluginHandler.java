package me.qyh.blog.plugin.imagevideolazyload;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.TemplateRenderHandlerRegistry;

public class ImageVideoLazyLoadPluginHandler extends PluginHandlerSupport {

	@Override
	public void addTemplateRenderHandler(TemplateRenderHandlerRegistry registry) {
		registry.register(new ImageVideoLazyLoadTemplateRenderHandler());
	}

}
