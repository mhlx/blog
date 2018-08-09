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
package me.qyh.blog.core.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.SpaceDao;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.event.LockDelEvent;
import me.qyh.blog.core.event.SpaceCreateEvent;
import me.qyh.blog.core.event.SpaceDelEvent;
import me.qyh.blog.core.event.SpaceUpdateEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.vo.SpaceQueryParam;

@Service
public class SpaceServiceImpl implements SpaceService, ApplicationEventPublisherAware, InitializingBean {

	@Autowired
	private SpaceDao spaceDao;
	@Autowired
	private LockManager lockManager;
	@Autowired
	private ArticleIndexer articleIndexer;

	private List<Space> cache = new CopyOnWriteArrayList<>();

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Space addSpace(Space space) throws LogicException {

		if (space.getIsPrivate()) {
			space.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(space.getLockId());
		}
		lockManager.ensureLockAvailable(space.getLockId());

		if (spaceDao.selectByAlias(space.getAlias()) != null) {
			throw new LogicException(
					new Message("space.alias.exists", "别名为" + space.getAlias() + "的空间已经存在了", space.getAlias()));
		}
		if (spaceDao.selectByName(space.getName()) != null) {
			throw new LogicException(
					new Message("space.name.exists", "名称为" + space.getName() + "的空间已经存在了", space.getName()));
		}
		space.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
		if (space.getIsDefault()) {
			spaceDao.resetDefault();
		}
		spaceDao.insert(space);

		this.applicationEventPublisher.publishEvent(new SpaceCreateEvent(this, space));

		Transactions.afterCommit(() -> {
			cache.add(space);
		});

		return space;
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Space updateSpace(Space space) throws LogicException {
		Space db = spaceDao.selectById(space.getId());
		if (db == null) {
			throw new LogicException("space.notExists", "空间不存在");
		}
		Space nameDb = spaceDao.selectByName(space.getName());
		if (nameDb != null && !nameDb.equals(db)) {
			throw new LogicException(
					new Message("space.name.exists", "名称为" + space.getName() + "的空间已经存在了", space.getName()));
		}
		// 如果空间是私有的，那么无法加锁
		if (space.getIsPrivate()) {
			space.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(space.getLockId());
		}

		if (space.getIsDefault()) {
			spaceDao.resetDefault();
		}

		spaceDao.update(space);

		db.setIsDefault(space.getIsDefault());
		db.setIsPrivate(space.getIsPrivate());
		db.setLockId(space.getLockId());
		db.setName(space.getName());

		Transactions.afterCommit(() -> {
			cache.replaceAll(replace -> {
				if (replace.getId().equals(space.getId())) {
					return db;
				}
				return replace;
			});
			articleIndexer.rebuildIndex();
		});

		this.applicationEventPublisher.publishEvent(new SpaceUpdateEvent(this, db, space));

		return space;
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, isolation = Isolation.SERIALIZABLE)
	public void deleteSpace(Integer id) throws LogicException {
		Space space = spaceDao.selectById(id);
		if (space == null) {
			throw new LogicException("space.notExists", "空间不存在");
		}
		if (space.getIsDefault()) {
			throw new LogicException("space.default.canNotDelete", "默认空间不能被删除");
		}
		// 推送空间删除事件，通知文章等删除
		this.applicationEventPublisher.publishEvent(new SpaceDelEvent(this, space));

		spaceDao.deleteById(id);

		Transactions.afterCommit(() -> {
			cache.removeIf(remove -> remove.getId().equals(id));
			articleIndexer.rebuildIndex();
		});
	}

	@Override
	public Optional<Space> getSpace(Integer id) {
		for (Space space : cache) {
			if (space.getId().equals(id)) {
				return Optional.of(new Space(space));
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Space> getSpace(String alias) {
		for (Space space : cache) {
			if (space.getAlias().equals(alias)) {
				return Optional.of(new Space(space));
			}
		}
		return Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Space> querySpace(SpaceQueryParam param) {
		if (param.getQueryPrivate() && !Environment.isLogin()) {
			param.setQueryPrivate(false);
		}
		return cache.stream().filter(space -> {
			if (!param.getQueryPrivate()) {
				return !space.getIsPrivate();
			}
			return true;
		}).map(Space::new).collect(Collectors.toList());
	}

	@EventListener
	public void handleLockDeleteEvent(LockDelEvent event) {
		spaceDao.deleteLock(event.getLock().getId());
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cache.addAll(spaceDao.selectByParam(new SpaceQueryParam()));
	}
}
