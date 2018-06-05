package me.qyh.blog.template.vo;

import me.qyh.blog.template.render.data.DataTagProcessor;

public class DataTagProcessorBean {

	private final String name;
	private final String dataName;
	private final boolean callable;

	public DataTagProcessorBean(DataTagProcessor<?> processor) {
		this.name = processor.getName();
		this.dataName = processor.getDataName();
		this.callable = processor.isCallable();
	}

	public String getName() {
		return name;
	}

	public String getDataName() {
		return dataName;
	}

	public boolean isCallable() {
		return callable;
	}

}
