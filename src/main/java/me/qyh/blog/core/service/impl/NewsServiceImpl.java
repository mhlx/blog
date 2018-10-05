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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.NewsDao;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.event.LockDelEvent;
import me.qyh.blog.core.event.NewsCreateEvent;
import me.qyh.blog.core.event.NewsDelEvent;
import me.qyh.blog.core.event.NewsUpdateEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.HitsStrategy;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.vo.NewsNav;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.core.vo.NewsStatistics;
import me.qyh.blog.core.vo.PageResult;

@Service
public class NewsServiceImpl implements NewsService, ApplicationEventPublisherAware, InitializingBean {

	@Autowired
	private NewsDao newsDao;
	@Autowired(required = false)
	private CommentServer commentServer;
	@Autowired(required = false)
	@Qualifier("newsHitsStrategy")
	private HitsStrategy<News> hitsStrategy;
	@Autowired
	private LockManager lockManager;

	private ApplicationEventPublisher applicationEventPublisher;

	private static final String COMMENT_MODULE_NAME = "news";

	@Override
	@Transactional(readOnly = true)
	public PageResult<News> queryNews(NewsQueryParam param) {
		if (!Environment.isLogin()) {
			param.setQueryPrivate(false);
		}
		List<News> newsList = newsDao.selectPage(param);
		setNewsComments(newsList);
		if (!Environment.isLogin()) {
			newsList.stream().filter(news -> news.getLockId() != null).forEach(news -> news.setContent(null));
		}
		return new PageResult<>(param, newsDao.selectCount(param), newsList);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void saveNews(News news) throws LogicException {
		if (news.getWrite() == null) {
			news.setWrite(Timestamp.valueOf(Times.now()));
		}
		if (news.getIsPrivate()) {
			news.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(news.getLockId());
		}
		newsDao.insert(news);
		applicationEventPublisher.publishEvent(new NewsCreateEvent(this, news));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void updateNews(News news) throws LogicException {
		News old = newsDao.selectById(news.getId());
		if (old == null) {
			throw new LogicException("news.notExists", "动态不存在");
		}

		if (news.getIsPrivate()) {
			news.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(news.getLockId());
		}

		news.setUpdate(Timestamp.valueOf(Times.now()));

		newsDao.update(news);
		applicationEventPublisher.publishEvent(new NewsUpdateEvent(this, old, news));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<News> getNews(Integer id) {
		News news = newsDao.selectById(id);
		if (news != null) {
			if (news.getIsPrivate()) {
				Environment.doAuthencation();
			}
			lockManager.openLock(news.getLockId());
			setNewsComments(news);
		}
		return Optional.ofNullable(news);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteNews(Integer id) throws LogicException {
		News old = newsDao.selectById(id);
		if (old == null) {
			throw new LogicException("news.notExists", "动态不存在");
		}
		newsDao.deleteById(id);

		commentServer.deleteComments(COMMENT_MODULE_NAME, id);
		applicationEventPublisher.publishEvent(new NewsDelEvent(this, List.of(old)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<News> queryLastNews(int limit, boolean queryLock) {
		List<News> newsList = newsDao.selectLast(limit, Environment.isLogin(), queryLock);
		setNewsComments(newsList);
		return newsList;
	}

	@Override
	@Transactional(readOnly = true)
	public NewsStatistics queryNewsStatistics() {
		return newsDao.selectStatistics(Environment.isLogin());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<NewsNav> getNewsNav(Integer id, boolean queryLock) {
		News news = newsDao.selectById(id);
		if (news == null) {
			return Optional.empty();
		}
		return getNewsNav(news, queryLock);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<NewsNav> getNewsNav(News news, boolean queryLock) {
		Objects.requireNonNull(news);
		if (news.getIsPrivate()) {
			Environment.doAuthencation();
		}
		boolean queryPrivate = Environment.isLogin();
		News previous = newsDao.getPreviousNews(news, queryPrivate, queryLock);
		News next = newsDao.getNextNews(news, queryPrivate, queryLock);
		if (previous == null && next == null) {
			return Optional.empty();
		}
		return Optional.of(new NewsNav(previous, next));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void hit(Integer id) {
		if (!Environment.isLogin()) {
			News news = newsDao.selectById(id);
			if (news != null) {
				lockManager.openLock(news.getLockId());
				hitsStrategy.hit(news);
			}
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	private void setNewsComments(List<News> newsList) {
		if (newsList.isEmpty()) {
			return;
		}
		Map<Integer, Integer> commentMap = commentServer.queryCommentNums(COMMENT_MODULE_NAME,
				newsList.stream().map(News::getId).collect(Collectors.toList()));

		newsList.forEach(news -> {
			Integer comment = commentMap.get(news.getId());
			news.setComments(comment == null ? 0 : comment);
		});
	}

	private void setNewsComments(News news) {
		if (news == null) {
			return;
		}
		news.setComments(commentServer.queryCommentNum(COMMENT_MODULE_NAME, news.getId()).orElse(0));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (commentServer == null) {
			commentServer = EmptyCommentServer.INSTANCE;
		}
		if (hitsStrategy == null) {
			hitsStrategy = new HitsStrategy<>() {

				@Override
				public void hit(News news) {
					synchronized (this) {
						newsDao.updateHits(news.getId(), newsDao.selectHits(news.getId()) + 1);
					}
				}
			};
		}
	}

	@EventListener
	public void handleLockDeleteEvent(LockDelEvent event) {
		newsDao.deleteLock(event.getLock().getId());
	}

}
