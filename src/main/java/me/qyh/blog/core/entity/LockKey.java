package me.qyh.blog.core.entity;

import java.io.Serializable;

/**
 * 开锁钥匙
 * 
 * @author Administrator
 *
 */
public interface LockKey extends Serializable {

	/**
	 * 锁ID
	 * 
	 * @return
	 */
	String lockId();

	/**
	 * 得到钥匙
	 * 
	 * @return 钥匙
	 */
	Serializable getKey();

}
