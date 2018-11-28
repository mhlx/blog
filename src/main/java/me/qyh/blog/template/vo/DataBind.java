package me.qyh.blog.template.vo;

import me.qyh.blog.core.exception.LogicException;

public abstract class DataBind {

	private String dataName;

	public DataBind(String dataName) {
		super();
		this.dataName = dataName;
	}

	public abstract Object getData() throws LogicException;

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

}
