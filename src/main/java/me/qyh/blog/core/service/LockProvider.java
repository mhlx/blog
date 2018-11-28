package me.qyh.blog.core.service;

import java.util.List;
import java.util.Optional;

import me.qyh.blog.core.entity.Lock;

/**
 * 锁提供器
 * 
 * <p>
 * <b>每个锁提供器的所有锁类型不能互相冲突，锁的ID也不能冲突</b>
 * </p>
 * 
 * @author wwwqyhme
 *
 */
public interface LockProvider {

	/**
	 * 获取所有的锁
	 * <p>
	 * 这个方法会被在只读事务中执行
	 * </p>
	 * 
	 * @return
	 */
	List<Lock> getAllLocks();

	/**
	 * 根据ID查询锁
	 * <p>
	 * 这个方法会被在只读事务中执行
	 * </p>
	 * 
	 * @param id
	 * @return
	 */
	Optional<Lock> getLock(String id);

}
