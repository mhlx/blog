package me.qyh.blog.core.entity;

import java.io.Serializable;

/**
 * 
 * @author Administrator
 *
 */
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private String password;
	private String email;

	/**
	 * gravatar头像 值为email的md5值，如果email为空，那么gravatar也为空
	 */
	private String gravatar;

	public User(User source) {
		this.name = source.name;
		this.password = source.password;
		this.email = source.email;
		this.gravatar = source.gravatar;
	}

	public User() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGravatar() {
		return gravatar;
	}

	public void setGravatar(String gravatar) {
		this.gravatar = gravatar;
	}

}
