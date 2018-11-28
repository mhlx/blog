package me.qyh.blog.plugin.comment.entity;

import java.io.Serializable;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

/**
 * 评论区域
 * 
 * @author Administrator
 *
 */
public class CommentModule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @since 5.6
	 */
	private String module;
	private Integer id;// 关联id

	public CommentModule(String module, Integer id) {
		super();
		this.module = module;
		this.id = id;
	}

	public CommentModule() {
		super();
	}

	/**
	 * @return the module
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(String module) {
		this.module = module;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(module, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			CommentModule rhs = (CommentModule) obj;
			return Objects.equals(this.module, rhs.module) && Objects.equals(this.id, rhs.id);
		}
		return false;
	}

}
