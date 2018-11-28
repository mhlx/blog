package me.qyh.blog.template.vo;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.PageQueryParam;

public class FragmentQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Boolean global;
	private Space space;
	private Boolean callable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getGlobal() {
		return global;
	}

	public void setGlobal(Boolean global) {
		this.global = global;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public Boolean getCallable() {
		return callable;
	}

	public void setCallable(Boolean callable) {
		this.callable = callable;
	}
//
}
