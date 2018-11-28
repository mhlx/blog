package me.qyh.blog.core.entity;

import java.io.Serializable;

/**
 * 
 * @author Administrator
 *
 */
public class BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Integer id;

	/**
	 * default
	 */
	public BaseEntity() {
		super();
	}

	/**
	 * @param id
	 *            entity id
	 */
	public BaseEntity(Integer id) {
		super();
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 是否拥有id
	 * 
	 * @return
	 */
	public boolean hasId() {
		return id != null;
	}
}
