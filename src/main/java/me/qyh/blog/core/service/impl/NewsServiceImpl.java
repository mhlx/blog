package me.qyh.blog.core.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.event.LockDelEvent;
import me.qyh.blog.core.event.NewsCreateEvent;
import me.qyh.blog.core.event.NewsDelEvent;
import me.qyh.blog.core.event.NewsUpdateEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.HitsStrategy;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.vo.NewsArchive;
import me.qyh.blog.core.vo.NewsArchivePageQueryParam;
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
	@Autowired
	private Markdown2Html markdown2Html;

	private ApplicationEventPublisher applicationEventPublisher;

	private static final String COMMENT_MODULE_NAME = "news";

	@Override
	@Transactional(readOnly = true)
	public PageResult<News> queryNews(NewsQueryParam param) {
		if (!Environment.hasAuthencated()) {
			param.setQueryPrivate(false);
		}
		List<News> newsList = newsDao.selectPage(param);
		setNewsComments(newsList);
		setNewsContent(newsList);
		if (!Environment.hasAuthencated()) {
			newsList.stream().filter(News::hasLock).forEach(news -> news.setContent(null));
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
		News old = newsDao.selectById(news.getId()).orElseThrow(() -> new LogicException("news.notExists", "动态不存在"));

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
		Optional<News> op = newsDao.selectById(id);
		if (op.isPresent()) {
			News news = op.get();
			if (news.getIsPrivate()) {
				Environment.doAuthencation();
			}
			lockManager.openLock(news.getLockId());
			setNewsComments(news);
			setNewsContent(news);
			return Optional.of(news);
		}
		return Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<News> getNewsForEdit(Integer id) {
		return newsDao.selectById(id);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteNews(Integer id) throws LogicException {
		News old = newsDao.selectById(id).orElseThrow(() -> new LogicException("news.notExists", "动态不存在"));
		newsDao.deleteById(id);

		commentServer.deleteComments(COMMENT_MODULE_NAME, id);
		applicationEventPublisher.publishEvent(new NewsDelEvent(this, List.of(old)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<News> queryLastNews(int limit, boolean queryLock) {
		List<News> newsList = newsDao.selectLast(limit, Environment.hasAuthencated(), queryLock);
		if (!Environment.hasAuthencated()) {
			newsList.stream().filter(News::hasLock).forEach(news -> news.setContent(null));
		}
		setNewsComments(newsList);
		setNewsContent(newsList);
		return newsList;
	}

	@Override
	@Transactional(readOnly = true)
	public NewsStatistics queryNewsStatistics() {
		return newsDao.selectStatistics(Environment.hasAuthencated());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<NewsNav> getNewsNav(Integer id, boolean queryLock) {
		Optional<News> op = newsDao.selectById(id);
		if (op.isEmpty()) {
			return Optional.empty();
		}
		return getNewsNav(op.get(), queryLock);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<NewsNav> getNewsNav(News news, boolean queryLock) {
		Objects.requireNonNull(news);
		if (news.getIsPrivate()) {
			Environment.doAuthencation();
		}
		lockManager.openLock(news.getLockId());
		boolean queryPrivate = Environment.hasAuthencated();
		News previous = newsDao.getPreviousNews(news, queryPrivate, queryLock).orElse(null);
		News next = newsDao.getNextNews(news, queryPrivate, queryLock).orElse(null);
		if (previous == null && next == null) {
			return Optional.empty();
		}
		if (previous != null) {
			setNewsComments(previous);
			setNewsContent(previous);
		}

		if (next != null) {
			setNewsComments(next);
			setNewsContent(next);
		}
		return Optional.of(new NewsNav(previous, next));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void hit(Integer id) {
		if (!Environment.hasAuthencated()) {
			Optional<News> op = newsDao.selectById(id);
			if (op.isPresent()) {
				News news = op.get();
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

	private void setNewsContent(News news) {
		if (Editor.MD.equals(news.getEditor())) {
			news.setContent(markdown2Html.toHtml(news.getContent()));
		}
	}

	private void setNewsContent(List<News> newsList) {
		if (newsList.isEmpty()) {
			return;
		}
		newsList.forEach(this::setNewsContent);
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
		if (newsDao.checkExistsByLockId(event.getLock().getId())) {
			throw new RuntimeLogicException(new Message("lock.delete.referenceByNewses", "锁已经被动态使用，无法删除"));
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<NewsArchive> queryNewsArchive(NewsArchivePageQueryParam param) {
		if (param.isQueryPrivate()) {
			param.setQueryPrivate(Environment.hasAuthencated());
		}

		int count = newsDao.selectNewsDaysCount(param);
		List<String> days = newsDao.selectNewsDays(param);
		int size = days.size();
		if (size == 0) {
			return new PageResult<>(param, count, new ArrayList<>());
		}
		Timestamp begin, end;
		if (size == 1) {
			String day = days.get(0);
			LocalDate localDate = LocalDate.parse(day);
			begin = Timestamp.valueOf(localDate.atStartOfDay());
			end = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay());
		} else {
			String max, min;
			if (param.isAsc()) {
				max = days.get(size - 1);
				min = days.get(0);
			} else {
				max = days.get(0);
				min = days.get(size - 1);
			}
			begin = Timestamp.valueOf(LocalDate.parse(min).atStartOfDay());
			end = Timestamp.valueOf(LocalDate.parse(max).plusDays(1).atStartOfDay());
		}

		NewsQueryParam np = new NewsQueryParam();
		np.setIgnorePaging(true);
		np.setBegin(begin);
		np.setEnd(end);
		np.setContent(param.getContent());
		np.setAsc(param.isAsc());
		np.setQueryPrivate(param.isQueryPrivate());

		List<News> newses = newsDao.selectPage(np);
		setNewsComments(newses);
		setNewsContent(newses);

		if (!Environment.hasAuthencated()) {
			newses.stream().filter(News::hasLock).forEach(news -> news.setContent(null));
		}

		Map<String, List<News>> map = newses.stream().collect(Collectors.groupingBy(news -> {
			Date date = news.getWrite();
			return Times.format(date, "yyyy-MM-dd");
		}));

		List<NewsArchive> archives = days.stream().map(d -> new NewsArchive(d, map.get(d)))
				.collect(Collectors.toList());

		return new PageResult<>(param, count, archives);
	}

}
