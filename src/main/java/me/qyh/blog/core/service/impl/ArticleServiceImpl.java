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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.ArticleDao;
import me.qyh.blog.core.dao.ArticleTagDao;
import me.qyh.blog.core.dao.SpaceDao;
import me.qyh.blog.core.dao.TagDao;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.ArticleTag;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.event.ArticleCreateEvent;
import me.qyh.blog.core.event.ArticleDelEvent;
import me.qyh.blog.core.event.ArticlePublishEvent;
import me.qyh.blog.core.event.ArticleUpdateEvent;
import me.qyh.blog.core.event.LockDelEvent;
import me.qyh.blog.core.event.SpaceDelEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.ArticleHitHandlerRegistry;
import me.qyh.blog.core.service.ArticleContentHandler;
import me.qyh.blog.core.service.ArticleHitHandler;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.HitsStrategy;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.text.CommonMarkdown2Html;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.ArticleArchiveTree;
import me.qyh.blog.core.vo.ArticleArchiveTree.ArticleArchiveMode;
import me.qyh.blog.core.vo.ArticleDetailStatistics;
import me.qyh.blog.core.vo.ArticleNav;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.ArticleStatistics;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.core.vo.TagCount;

public class ArticleServiceImpl
		implements ArticleService, ArticleHitHandlerRegistry, InitializingBean, ApplicationEventPublisherAware {

	@Autowired
	private ArticleDao articleDao;
	@Autowired
	private SpaceDao spaceDao;
	@Autowired
	private ArticleTagDao articleTagDao;
	@Autowired
	private TagDao tagDao;
	@Autowired
	private LockManager lockManager;
	@Autowired(required = false)
	private CommentServer commentServer;
	@Autowired
	private PlatformTransactionManager transactionManager;
	@Autowired
	private ArticleIndexer articleIndexer;

	private ApplicationEventPublisher applicationEventPublisher;

	private boolean rebuildIndex = true;

	@Autowired
	private ArticleContentHandler articleContentHandler;
	private final ScheduleManager scheduleManager = new ScheduleManager();

	/**
	 * 点击策略
	 */
	@Autowired(required = false)
	@Qualifier("articleHitsStrategy")
	private HitsStrategy<Article> hitsStrategy;

	private ArticleHitManager articleHitManager;

	@Autowired(required = false)
	private Markdown2Html markdown2Html;

	private List<ArticleHitHandler> hitHandlers = new ArrayList<>();

	/**
	 * @since 6.5
	 */
	private int publishSchedulePeriodSec;
	@Autowired
	private TaskScheduler taskScheduler;

	@Override
	@Transactional(readOnly = true)
	public Optional<Article> getArticleForView(String idOrAlias) {
		Optional<Article> optionalArticle = getCheckedArticle(idOrAlias);
		if (optionalArticle.isPresent()) {

			Article article = optionalArticle.get();
			article.setComments(commentServer.queryCommentNum(COMMENT_MODULE_NAME, article.getId()).orElse(0));

			String content = article.getContent();

			if (Editor.MD.equals(article.getEditor())) {
				content = markdown2Html.toHtml(content);
				article.setSummary(markdown2Html.toHtml(article.getSummary()));
			}

			content = articleContentHandler.handle(content);

			article.setContent(content);

			return Optional.of(article);
		}
		return Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Article> getArticleForEdit(Integer id) {
		Article article = articleDao.selectById(id);
		return Optional.ofNullable(article);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void hit(Integer id) {
		articleHitManager.hit(id);
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Article updateArticle(Article article) throws LogicException {
		Space space = getRequiredSpace(article.getSpace().getId());
		article.setSpace(space);
		// 如果文章是私有的，无法设置锁
		if (article.isPrivate()) {
			article.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(article.getLockId());
		}
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		Article articleDb = articleDao.selectById(article.getId());
		if (articleDb == null) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		if (articleDb.isDeleted()) {
			throw new LogicException("article.deleted", "文章已经被删除");
		}
		if (article.getAlias() != null) {
			Article aliasDb = articleDao.selectByAlias(article.getAlias());
			if (aliasDb != null && !aliasDb.equals(article)) {
				throw new LogicException("article.alias.exists", "别名" + article.getAlias() + "已经存在",
						article.getAlias());
			}
		}

		if (nochange(article, articleDb)) {
			return articleDb;
		}

		Timestamp pubDate = null;
		switch (article.getStatus()) {
		case DRAFT:
			pubDate = articleDb.isSchedule() ? null : articleDb.getPubDate();
			break;
		case PUBLISHED:
			pubDate = articleDb.isSchedule() ? now : articleDb.getPubDate() != null ? articleDb.getPubDate() : now;
			break;
		case SCHEDULED:
			pubDate = article.getPubDate();
			break;
		default:
			break;
		}

		article.setPubDate(pubDate);

		if (articleDb.getPubDate() != null && article.isPublished()) {
			article.setLastModifyDate(now);
		}

		articleTagDao.deleteByArticle(articleDb);

		articleDao.update(article);

		boolean rebuildIndexWhenTagChange = insertTags(article);

		Transactions.afterCommit(() -> {

			if (article.isSchedule()) {
				scheduleManager.update();
			}

			if (rebuildIndexWhenTagChange) {
				articleIndexer.rebuildIndex();
			} else {
				articleIndexer.deleteDocument(article.getId());
				if (article.isPublished()) {
					articleIndexer.addOrUpdateDocument(article.getId());
				}
			}
		});
		applicationEventPublisher
				.publishEvent(new ArticleUpdateEvent(this, articleDb, articleDao.selectById(article.getId())));
		return article;

	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Article writeArticle(Article article) throws LogicException {
		Space space = getRequiredSpace(article.getSpace().getId());
		article.setSpace(space);
		// 如果文章是私有的，无法设置锁
		if (article.isPrivate()) {
			article.setLockId(null);
		} else {
			lockManager.ensureLockAvailable(article.getLockId());
		}
		if (article.getAlias() != null) {
			Article aliasDb = articleDao.selectByAlias(article.getAlias());
			if (aliasDb != null) {
				throw new LogicException("article.alias.exists", "别名" + article.getAlias() + "已经存在",
						article.getAlias());
			}
		}

		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		Timestamp pubDate = null;
		switch (article.getStatus()) {
		case DRAFT:
			// 如果是草稿
			pubDate = null;
			break;
		case PUBLISHED:
			pubDate = now;
			break;
		case SCHEDULED:
			pubDate = article.getPubDate();
			break;
		default:
			break;
		}
		article.setPubDate(pubDate);

		articleDao.insert(article);

		boolean rebuildIndexWhenTagChange = insertTags(article);
		if (article.isSchedule()) {
			scheduleManager.update();
		}

		Transactions.afterCommit(() -> {
			if (rebuildIndexWhenTagChange) {
				articleIndexer.rebuildIndex();
			} else {
				if (article.isPublished()) {
					articleIndexer.addOrUpdateDocument(article.getId());
				}
			}
		});
		applicationEventPublisher.publishEvent(new ArticleCreateEvent(this, articleDao.selectById(article.getId())));
		return article;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Article publishDraft(Integer id) throws LogicException {
		Article article = articleDao.selectById(id);
		if (article == null) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		if (!article.isDraft()) {
			throw new LogicException("article.notDraft", "文章已经被删除");
		}
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		article.setPubDate(article.isSchedule() ? now : article.getPubDate() != null ? article.getPubDate() : now);
		article.setStatus(ArticleStatus.PUBLISHED);
		articleDao.update(article);
		Transactions.afterCommit(() -> articleIndexer.addOrUpdateDocument(id));
		applicationEventPublisher.publishEvent(new ArticlePublishEvent(this, List.of(article)));
		return article;
	}

	private boolean insertTags(Article article) {
		Set<Tag> tags = article.getTags();
		boolean rebuildIndexWhenTagChange = false;
		Set<String> indexTags = new HashSet<>();
		if (!CollectionUtils.isEmpty(tags)) {
			for (Tag tag : tags) {
				String tagName = cleanTag(tag.getName());
				Tag tagDb = tagDao.selectByName(tagName);
				ArticleTag articleTag = new ArticleTag();
				articleTag.setArticle(article);
				if (tagDb == null) {
					// 插入标签
					tag.setCreate(Timestamp.valueOf(LocalDateTime.now()));
					tag.setName(tagName);
					tagDao.insert(tag);
					articleTag.setTag(tag);
					indexTags.add(tagName);
					rebuildIndexWhenTagChange = true;
				} else {
					articleTag.setTag(tagDb);
				}
				articleTagDao.insert(articleTag);
			}
		}
		if (!indexTags.isEmpty()) {
			Transactions.afterCommit(() -> articleIndexer.addTags(indexTags.toArray(String[]::new)));
		}
		return rebuildIndexWhenTagChange;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Article> queryArticle(ArticleQueryParam param) {
		checkParam(param);

		// 5.5.5
		if (Validators.isEmptyOrNull(param.getTag(), true)) {
			param.setTagId(null);
		} else {
			Tag tag = tagDao.selectByName(param.getTag());
			if (tag == null) {
				return new PageResult<>(param, 0, new ArrayList<>());
			}
			param.setTagId(tag.getId());
		}

		PageResult<Article> page;
		if (param.hasQuery()) {
			page = articleIndexer.query(param);
		} else {
			List<Article> datas = articleDao.selectPage(param);
			int count;
			if (param.isIgnorePaging()) {
				count = datas.size();
			} else {
				count = articleDao.selectCount(param);
			}
			page = new PageResult<>(param, count, datas);
		}
		// query comments
		List<Article> datas = page.getDatas();
		if (!CollectionUtils.isEmpty(datas)) {
			List<Integer> ids = datas.stream().map(Article::getId).collect(Collectors.toList());
			Map<Integer, Integer> countsMap = commentServer.queryCommentNums(COMMENT_MODULE_NAME, ids);
			Map<Integer, String> htmlMap = markdown2Html
					.toHtmls(datas.stream().filter(article -> Editor.MD.equals(article.getEditor()))
							.collect(Collectors.toMap(Article::getId, Article::getSummary)));
			datas.stream().forEach(article -> {
				Integer comments = countsMap.get(article.getId());
				article.setComments(comments == null ? 0 : comments);
				if (Editor.MD.equals(article.getEditor())) {
					article.setSummary(htmlMap.get(article.getId()));
				}
			});
			if (!Environment.isLogin()) {
				datas.stream().filter(Article::hasLock).forEach(art -> art.setSummary(null));
			}
		}
		return page;
	}

	private void checkParam(ArticleQueryParam param) {
		// 如果查询私有文章，但是用户没有登录
		if (param.isQueryPrivate() && !Environment.isLogin()) {
			param.setQueryPrivate(false);
		}
		// 如果在空间下查询，不能查询多个空间
		if (param.getSpace() != null) {
			param.setSpaceIds(new HashSet<>());
		}
		// 如果查询多个空间
		if (!param.getSpaces().isEmpty()) {
			// 如果空间别名与alias一致，查询这个空间
			Set<Integer> includeSpaceIds = new HashSet<>();
			SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
			spaceQueryParam.setQueryPrivate(param.isQueryPrivate());
			out: for (Space space : spaceDao.selectByParam(spaceQueryParam)) {
				for (String alias : param.getSpaces()) {
					if (alias.equals(space.getAlias())) {
						includeSpaceIds.add(space.getId());
						continue out;
					}
				}
			}
			param.setSpaceIds(includeSpaceIds);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Article logicDeleteArticle(Integer id) throws LogicException {
		Article article = articleDao.selectById(id);
		if (article == null) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		if (article.isDeleted()) {
			throw new LogicException("article.deleted", "文章已经被删除");
		}
		article.setStatus(ArticleStatus.DELETED);
		articleDao.update(article);

		Transactions.afterCommit(() -> {
			articleIndexer.deleteDocument(id);
		});

		applicationEventPublisher.publishEvent(new ArticleDelEvent(this, List.of(article), true));

		return article;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Article recoverArticle(Integer id) throws LogicException {
		Article article = articleDao.selectById(id);
		if (article == null) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		if (!article.isDeleted()) {
			throw new LogicException("article.undeleted", "文章未删除");
		}
		Article old = new Article(article);
		ArticleStatus status = ArticleStatus.PUBLISHED;
		if (article.getPubDate().after(Timestamp.valueOf(LocalDateTime.now()))) {
			status = ArticleStatus.SCHEDULED;
		}
		article.setStatus(status);
		articleDao.update(article);

		Transactions.afterCommit(() -> articleIndexer.addOrUpdateDocument(id));

		applicationEventPublisher.publishEvent(new ArticleUpdateEvent(this, old, article));
		return article;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteArticle(Integer id) throws LogicException {
		Article article = articleDao.selectById(id);
		if (article == null) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		if (!article.isDraft() && !article.isDeleted()) {
			throw new LogicException("article.undeleted", "文章未删除");
		}
		// 删除博客的引用
		articleTagDao.deleteByArticle(article);
		articleDao.deleteById(id);
		commentServer.deleteComments(COMMENT_MODULE_NAME, id);

		applicationEventPublisher.publishEvent(new ArticleDelEvent(this, List.of(article), false));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * <b>这个方法只提供根据发布时间排序的上下文章</b>
	 * </p>
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ArticleNav> getArticleNav(String idOrAlias, boolean queryLock) {
		Optional<Article> optionalArticle = getCheckedArticle(idOrAlias);
		if (optionalArticle.isPresent()) {
			Article article = optionalArticle.get();
			return getArticleNav(article, queryLock);
		}
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * <b>这个方法只提供根据发布时间排序的上下文章</b>
	 * </p>
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ArticleNav> getArticleNav(Article article, boolean queryLock) {
		Objects.requireNonNull(article);
		if (!Environment.match(article.getSpace())) {
			return Optional.empty();
		}
		boolean queryPrivate = Environment.isLogin();
		Article previous = articleDao.getPreviousArticle(article, queryPrivate, queryLock);
		Article next = articleDao.getNextArticle(article, queryPrivate, queryLock);
		return (previous != null || next != null) ? Optional.of(new ArticleNav(previous, next)) : Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TagCount> queryTags() {
		return articleTagDao.selectTags(Environment.getSpace(), Environment.isLogin());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Article> selectRandom(boolean queryLock) {
		return Optional.ofNullable(articleDao.selectRandom(Environment.getSpace(), Environment.isLogin(), queryLock));
	}

	@Override
	@Transactional(readOnly = true)
	public ArticleArchiveTree selectArticleArchives(ArticleArchiveMode mode) {
		List<Article> articles = articleDao.selectSimplePublished(Environment.getSpace(), Environment.isLogin());
		return new ArticleArchiveTree(articles, mode == null ? ArticleArchiveMode.YMD : mode);
	}

	@Override
	@Transactional(readOnly = true)
	public ArticleDetailStatistics queryArticleDetailStatistics(Space space) {
		ArticleDetailStatistics articleDetailStatistics = new ArticleDetailStatistics(
				articleDao.selectAllStatistics(space));
		ArticleQueryParam param = new ArticleQueryParam();
		param.setQueryPrivate(true);
		param.setSpace(space);
		Map<ArticleStatus, Integer> countMap = new EnumMap<>(ArticleStatus.class);
		for (ArticleStatus status : ArticleStatus.values()) {
			param.setStatus(status);
			countMap.put(status, articleDao.selectCount(param));
		}
		articleDetailStatistics.setStatusCountMap(countMap);
		return articleDetailStatistics;
	}

	@Override
	@Transactional(readOnly = true)
	public ArticleStatistics queryArticleStatistics() {
		ArticleStatistics articleStatistics = articleDao.selectStatistics(Environment.getSpace(),
				Environment.isLogin());
		if (!Environment.hasSpace()) {
			articleStatistics.setSpaceStatisticsList(articleDao.selectArticleSpaceStatistics(Environment.isLogin()));
		}
		return articleStatistics;
	}

	@Override
	public void preparePreview(Article article) {
		String content = article.getContent();
		if (Editor.MD.equals(article.getEditor())) {
			content = markdown2Html.toHtml(content);
		}
		if (articleContentHandler != null) {
			content = articleContentHandler.handlePreview(content);
		}
		article.setContent(content);
	}

	@EventListener
	public void handleLockDeleteEvent(LockDelEvent event) {
		articleDao.deleteLock(event.getLock().getId());
	}

	@EventListener
	public void handleSpaceDeleteEvent(SpaceDelEvent event) {
		Space deleted = event.getSpace();
		// 查询该空间下是否存在文章
		int count = articleDao.selectCountBySpace(deleted);
		if (count > 0) {
			// 空间下存在文章
			// 移动到默认空间
			Space defaultSpace = spaceDao.selectDefault();
			// 默认空间不存在，无法删除空间
			if (defaultSpace == null) {
				throw new RuntimeLogicException(new Message("space.delete.hasArticles", "空间下存在文章，删除失败"));
			}

			articleDao.moveSpace(deleted, defaultSpace);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (rebuildIndex) {
			this.articleIndexer.rebuildIndex();
		}

		if (hitsStrategy == null) {
			hitsStrategy = new DefaultHitsStrategy();
		}

		this.articleHitManager = new ArticleHitManager(hitsStrategy);

		scheduleManager.update();

		if (commentServer == null) {
			commentServer = EmptyCommentServer.INSTANCE;
		}

		if (markdown2Html == null) {
			markdown2Html = CommonMarkdown2Html.INSTANCE;
		}

		if (publishSchedulePeriodSec <= 0) {
			publishSchedulePeriodSec = 5;
		}

		taskScheduler.scheduleAtFixedRate(scheduleManager::publish, publishSchedulePeriodSec * 1000L);
	}

	private Optional<Article> getCheckedArticle(String idOrAlias) {
		Article article;
		try {
			int id = Integer.parseInt(idOrAlias);
			article = articleDao.selectById(id);
		} catch (NumberFormatException e) {
			article = articleDao.selectByAlias(idOrAlias);
		}
		if (article != null && article.isPublished() && Environment.match(article.getSpace())) {
			if (article.isPrivate()) {
				Environment.doAuthencation();
			}

			lockManager.openLock(article.getLockId());
			return Optional.of(article);
		}

		return Optional.empty();
	}

	/**
	 * 查询标签是否存在的时候清除两边空格并且忽略大小写
	 * 
	 * @param tag
	 * @return
	 */
	protected String cleanTag(String tag) {
		return tag.strip().toLowerCase();
	}

	public void setRebuildIndex(boolean rebuildIndex) {
		this.rebuildIndex = rebuildIndex;
	}

	private final class ScheduleManager {
		private Timestamp start;

		public int publish() {
			if (start == null) {
				return 0;
			}
			long now = System.currentTimeMillis();
			if (now < start.getTime()) {
				return 0;
			} else {
				Timestamp startCopy = new Timestamp(start.getTime());
				List<Article> articles = Transactions.executeInTransaction(transactionManager, status -> {
					Transactions.afterRollback(() -> start = startCopy);
					List<Article> schedules = articleDao.selectScheduled(new Timestamp(now));
					if (!schedules.isEmpty()) {
						for (Article article : schedules) {
							article.setStatus(ArticleStatus.PUBLISHED);
							articleDao.update(article);
						}
						applicationEventPublisher.publishEvent(new ArticlePublishEvent(this, schedules));
					}
					start = articleDao.selectMinimumScheduleDate();
					return schedules;
				});
				articleIndexer.addOrUpdateDocument(articles.stream().map(Article::getId).toArray(Integer[]::new));
				return articles.size();
			}
		}

		void update() {
			Transactions.executeInReadOnlyTransaction(transactionManager, status -> {
				start = articleDao.selectMinimumScheduleDate();
			});
		}
	}

	private final class ArticleHitManager {

		private final HitsStrategy<Article> hitsStrategy;

		ArticleHitManager(HitsStrategy<Article> hitsStrategy) {
			super();
			this.hitsStrategy = hitsStrategy;
		}

		void hit(Integer id) {
			Article article = articleDao.selectById(id);
			if (article != null && validHit(article)) {

				if (!hitHandlers.isEmpty()) {
					for (ArticleHitHandler hitHandler : hitHandlers) {
						hitHandler.hit(article);
					}
				}

				hitsStrategy.hit(article);
			}
		}

		private boolean validHit(Article article) {
			boolean hit = !Environment.isLogin() && article.isPublished() && Environment.match(article.getSpace())
					&& !article.getIsPrivate();

			if (hit) {
				lockManager.openLock(article.getLockId());
			}
			return hit;
		}
	}

	/**
	 * 默认文章点击策略，文章的点击数将会实时显示
	 * <p>
	 * <b>这种策略下每次点击都会增加点击量</b>
	 * </p>
	 * 
	 * @author mhlx
	 *
	 */
	private final class DefaultHitsStrategy implements HitsStrategy<Article> {

		@Override
		public void hit(Article article) {
			synchronized (this) {
				Transactions.executeInTransaction(transactionManager, status -> {
					Integer id = article.getId();
					int hits = articleDao.selectHits(id) + 1;
					articleDao.updateHits(id, hits);

					Transactions.afterCommit(() -> {
						articleIndexer.updateHits(Map.of(id, hits));
					});
				});
			}
		}
	}

	/**
	 * 判断文章是否需要更新
	 * 
	 * @param newArticle
	 *            当前文章
	 * @param old
	 *            已经存在的文章
	 * @return
	 */
	protected boolean nochange(Article newArticle, Article old) {
		Objects.requireNonNull(newArticle);
		Objects.requireNonNull(old);
		return Objects.equals(newArticle.getAlias(), old.getAlias())
				&& Objects.equals(newArticle.getAllowComment(), old.getAllowComment())
				&& Objects.equals(newArticle.getContent(), old.getContent())
				&& Objects.equals(newArticle.getFeatureImage(), old.getFeatureImage())
				&& Objects.equals(newArticle.getFrom(), old.getFrom())
				&& Objects.equals(newArticle.getIsPrivate(), old.getIsPrivate())
				&& Objects.equals(newArticle.getLevel(), old.getLevel())
				&& Objects.equals(newArticle.getLockId(), old.getLockId())
				&& Objects.equals(newArticle.getSpace(), old.getSpace())
				&& Objects.equals(newArticle.getSummary(), old.getSummary())
				&& Objects.equals(newArticle.getTagStr(), old.getTagStr())
				&& Objects.equals(newArticle.getTitle(), old.getTitle())
				&& Objects.equals(newArticle.getStatus(), old.getStatus());
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public ArticleHitHandlerRegistry register(ArticleHitHandler handler) {
		this.hitHandlers.add(handler);
		return this;
	}

	private Space getRequiredSpace(Integer id) throws LogicException {
		Space space = spaceDao.selectById(id);
		if (space == null) {
			throw new LogicException("space.notExists", "空间不存在");
		}
		return space;
	}

	public void setPublishSchedulePeriodSec(int publishSchedulePeriodSec) {
		this.publishSchedulePeriodSec = publishSchedulePeriodSec;
	}

}
