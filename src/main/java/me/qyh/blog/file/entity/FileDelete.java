package me.qyh.blog.file.entity;

import me.qyh.blog.core.entity.BaseEntity;
import me.qyh.blog.file.entity.BlogFile.BlogFileType;

/**
 * 
 * @author Administrator
 *
 */
public class FileDelete extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private BlogFileType type;
	private Integer store;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public BlogFileType getType() {
		return type;
	}

	public void setType(BlogFileType type) {
		this.type = type;
	}

	public Integer getStore() {
		return store;
	}

	public void setStore(Integer store) {
		this.store = store;
	}

}
