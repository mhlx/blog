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
