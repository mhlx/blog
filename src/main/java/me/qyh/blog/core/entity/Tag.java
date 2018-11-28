package me.qyh.blog.core.entity;

import java.sql.Timestamp;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

/**
 * 
 * @author Administrator
 *
 */
public class Tag extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Timestamp create;

	/**
	 * default
	 */
	public Tag() {
		super();
	}

	/**
	 * @param name
	 *            标签名
	 */
	public Tag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getCreate() {
		return create;
	}

	public void setCreate(Timestamp create) {
		this.create = create;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			Tag rhs = (Tag) obj;
			return Objects.equals(this.name, rhs.name);
		}
		return false;
	}
}
