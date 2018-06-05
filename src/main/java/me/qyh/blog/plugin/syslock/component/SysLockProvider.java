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
package me.qyh.blog.plugin.syslock.component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.event.LockCreateEvent;
import me.qyh.blog.core.event.LockDelEvent;
import me.qyh.blog.core.event.LockUpdateEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.BCrypts;
import me.qyh.blog.core.service.LockProvider;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.plugin.syslock.dao.SysLockDao;
import me.qyh.blog.plugin.syslock.entity.PasswordLock;
import me.qyh.blog.plugin.syslock.entity.SysLock;
import me.qyh.blog.plugin.syslock.entity.SysLock.SysLockType;

/**
 * 系统锁管理
 * 
 * @author Administrator
 *
 */
@Component
public class SysLockProvider implements LockProvider, ApplicationEventPublisherAware {

	@Autowired
	private SysLockDao sysLockDao;

	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 删除锁
	 * 
	 * @param id
	 *            锁id
	 */
	@CacheEvict(value = "lockCache", key = "'lock-'+#id")
	public void removeLock(String id) {
		Lock lock = sysLockDao.selectById(id);
		if (lock != null) {
			sysLockDao.delete(id);
			applicationEventPublisher.publishEvent(new LockDelEvent(this, lock));
		}
	}

	/**
	 * 新增系统锁
	 * 
	 * @param lock
	 *            待新增的系统锁
	 */
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
	public SysLock addLock(SysLock lock) {
		lock.setId(StringUtils.uuid());
		lock.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
		encryptPasswordLock(lock);
		sysLockDao.insert(lock);
		applicationEventPublisher.publishEvent(new LockCreateEvent(this, lock));
		return lock;
	}

	/**
	 * 更新系统锁
	 * 
	 * @param lock
	 *            待更新的锁
	 * @throws LogicException
	 *             逻辑异常：锁不存在|锁类型不匹配
	 */
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
	@CacheEvict(value = "lockCache", key = "'lock-'+#lock.id")
	public SysLock updateLock(SysLock lock) throws LogicException {
		SysLock db = sysLockDao.selectById(lock.getId());
		if (db == null) {
			throw new LogicException("lock.notexists", "锁不存在，可能已经被删除");
		}
		if (!db.getType().equals(lock.getType())) {
			throw new LogicException("lock.type.unmatch", "锁类型不匹配");
		}
		encryptPasswordLock(lock);
		sysLockDao.update(lock);
		applicationEventPublisher.publishEvent(new LockUpdateEvent(this, db, lock));
		return lock;
	}

	private void encryptPasswordLock(SysLock lock) {
		if (SysLockType.PASSWORD.equals(lock.getType())) {
			PasswordLock plock = (PasswordLock) lock;
			plock.setPassword(BCrypts.encode(plock.getPassword()));
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public List<Lock> getAllLocks() {
		return Collections.unmodifiableList(sysLockDao.selectAll());
	}

	@Override
	@Cacheable(value = "lockCache", key = "'lock-'+#id")
	public Optional<Lock> getLock(String id) {
		return Optional.ofNullable(sysLockDao.selectById(id));
	}
}
