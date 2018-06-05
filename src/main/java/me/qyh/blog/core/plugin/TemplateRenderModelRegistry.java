package me.qyh.blog.core.plugin;

public interface TemplateRenderModelRegistry {

	TemplateRenderModelRegistry registry(String key, Object value) throws Exception;

}
