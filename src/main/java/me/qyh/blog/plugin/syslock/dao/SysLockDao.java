package me.qyh.blog.plugin.syslock.dao;

import java.util.List;
import java.util.Optional;

import me.qyh.blog.plugin.syslock.entity.SysLock;

/**
 * 
 * @author Administrator
 *
 */
public interface SysLockDao {

	/**
	 * 获取所有系统锁
	 * 
	 * @return 所有系统所
	 */
	List<SysLock> selectAll();

	/**
	 * 根据id删除系统锁
	 * 
	 * @param id
	 *            系统锁id
	 */
	void delete(String id);

	/**
	 * 插入系统锁
	 * 
	 * @param lock
	 *            待插入的系统锁
	 */
	void insert(SysLock lock);

	/**
	 * 更新系统锁
	 * 
	 * @param lock
	 *            待更新的系统锁
	 */
	void update(SysLock lock);

	/**
	 * 根据id查询系统锁
	 * 
	 * @param id
	 *            锁id
	 * @return 如果不存在，返回null
	 */
	Optional<SysLock> selectById(String id);

}
