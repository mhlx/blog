package me.qyh.blog.core.entity;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.exception.LogicException;

/**
 * 锁，如果用户为对象设置了锁，那么访问的时候需要解锁才能访问(非登录用户)
 * 
 * @author Administrator
 *
 */
public abstract class Lock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;

	/**
	 * 从请求中获取钥匙
	 * 
	 * @param request
	 *            当前请求
	 */
	public abstract LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException;

	/**
	 * 开锁
	 * 
	 * @param key
	 *            钥匙
	 */
	public abstract void tryOpen(LockKey key) throws LogicException;

	/**
	 * 获取锁类型(用于模板)
	 * 
	 * @return 锁类型
	 */
	public abstract String getLockType();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
