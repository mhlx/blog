package me.qyh.blog.core.plugin;

import me.qyh.blog.template.render.NamedTemplateRenderHandler;
import me.qyh.blog.template.render.TemplateRenderHandler;

public interface TemplateRenderHandlerRegistry {

	TemplateRenderHandlerRegistry register(TemplateRenderHandler handler);

	TemplateRenderHandlerRegistry register(NamedTemplateRenderHandler handler);
}
