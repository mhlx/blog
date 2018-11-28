package me.qyh.blog.core.plugin;

import me.qyh.blog.template.render.data.DataTagProcessor;

public interface DataTagProcessorRegistry {

	DataTagProcessorRegistry register(DataTagProcessor<?> processor);

}
