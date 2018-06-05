/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.plugin.syslock.dao;

import java.util.List;

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
	SysLock selectById(String id);

}
