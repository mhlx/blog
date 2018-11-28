package me.qyh.blog.plugin.syslock.entity;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;

/**
 * 系统锁
 * 
 * @author Administrator
 *
 */
public class SysLock extends Lock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SysLockType type;
	private Timestamp createDate;

	/**
	 * 锁类型
	 * 
	 * @author Administrator
	 *
	 */
	public enum SysLockType {
		PASSWORD, // 密码锁
		QA// 问答锁
	}

	/**
	 * default
	 */
	public SysLock() {
		super();
	}

	protected SysLock(SysLockType type) {
		this.type = type;
	}

	public SysLockType getType() {
		return type;
	}

	@Override
	public LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException {
		throw new SystemException("不支持的操作");
	}

	@Override
	public void tryOpen(LockKey key) throws LogicException {
		throw new SystemException("不支持的操作");
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	@Override
	public String getLockType() {
		return type.name().toLowerCase();
	}
}
