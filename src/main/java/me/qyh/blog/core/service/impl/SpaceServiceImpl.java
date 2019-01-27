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
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.exception.RuntimeLogicException;
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

	private final List<Space> cache = new CopyOnWriteArrayList<>();

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

		Transactions.afterCommit(() -> cache.add(space));

		return space;
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Space updateSpace(Space space) throws LogicException {
		Optional<Space> op = spaceDao.selectById(space.getId());
		if (op.isEmpty()) {
			throw new ResourceNotFoundException("space.notExists", "空间不存在");
		}
		Space db = op.get();
		if (spaceDao.selectByName(space.getName()).filter(nameDb -> !nameDb.equals(db)).isPresent()) {
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

		boolean resetDefault = space.getIsDefault();
		Transactions.afterCommit(() -> {
			cache.replaceAll(replace -> {
				if (replace.getId().equals(space.getId())) {
					return db;
				} else {
					if (resetDefault) {
						replace.setIsDefault(false);
					}
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
		Optional<Space> op = spaceDao.selectById(id);
		if (op.isEmpty()) {
			throw new ResourceNotFoundException("space.notExists", "空间不存在");
		}
		Space space = op.get();
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
		if (param.getQueryPrivate() && !Environment.hasAuthencated()) {
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
		if (spaceDao.checkExistsByLockId(event.getLock().getId())) {
			throw new RuntimeLogicException(new Message("lock.delete.referenceBySpaces", "锁已经被空间使用，无法删除"));
		}
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
