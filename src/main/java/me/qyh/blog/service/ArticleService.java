package me.qyh.blog.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.StringUtils;

import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Markdown2Html;
import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.entity.ArticleCategory;
import me.qyh.blog.entity.ArticleTag;
import me.qyh.blog.entity.Category;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.entity.Tag;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.ArticleCategoryMapper;
import me.qyh.blog.mapper.ArticleMapper;
import me.qyh.blog.mapper.ArticleTagMapper;
import me.qyh.blog.mapper.CommentMapper;
import me.qyh.blog.security.SecurityChecker;
import me.qyh.blog.service.SimpleCacheManager.SimpleCache;
import me.qyh.blog.service.event.CategoryDeleteEvent;
import me.qyh.blog.service.event.TagDeleteEvent;
import me.qyh.blog.utils.JsoupUtils;
import me.qyh.blog.vo.ArticleArchive;
import me.qyh.blog.vo.ArticleArchiveQueryParam;
import me.qyh.blog.vo.ArticleCategoryStatistic;
import me.qyh.blog.vo.ArticleQueryParam;
import me.qyh.blog.vo.ArticleStatistic;
import me.qyh.blog.vo.ArticleStatusStatistic;
import me.qyh.blog.vo.ArticleTagStatistic;
import me.qyh.blog.vo.HandledArticleQueryParam;
import me.qyh.blog.vo.PageResult;

@Service
public class ArticleService implements CommentModuleHandler<Article> {

	private final ArticleIndexer articleIndexer;
	private final ArticleMapper articleMapper;
	private final Markdown2Html markdown2Html;
	private final ScheduleManager scheduleManager;
	private final ArticleTagMapper articleTagMapper;
	private final ArticleCategoryMapper articleCategoryMapper;
	private final CommentMapper commentMapper;

	private SimpleCache<Category> categoryCache = SimpleCacheManager.get().getCache(CategoryService.class.getName());
	private SimpleCache<Tag> tagCache = SimpleCacheManager.get().getCache(TagService.class.getName());

	private static final Logger logger = LoggerFactory.getLogger(ArticleService.class.getName());

	private static final String COMMENT_MODULE_NAME = "article";

	public ArticleService(ArticleMapper articleMapper, Markdown2Html markdown2Html,
			PlatformTransactionManager transactionManager, ArticleTagMapper articleTagMapper,
			ArticleCategoryMapper articleCategoryMapper, BlogProperties blogProperties, CommentMapper commentMapper)
			throws IOException {
		this.articleIndexer = new ArticleIndexer();
		this.articleMapper = articleMapper;
		this.markdown2Html = markdown2Html;
		this.articleTagMapper = articleTagMapper;
		this.articleCategoryMapper = articleCategoryMapper;
		this.scheduleManager = new ScheduleManager(transactionManager);
		this.commentMapper = commentMapper;
		if (blogProperties.isRebuildIndexWhenStartup()) {
			try {
				List<Article> articles = articleMapper.selectPublished();
				makeArticlesIndexable(articles);
				articleIndexer.rebuild(articles);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateArticle(Article article) {
		Optional<Article> opOld = articleMapper.selectById(article.getId());
		if (opOld.isEmpty()) {
			throw new LogicException("articleService.update.notExists", "文章不存在");
		}
		if (article.getAlias() != null) {
			Optional<Article> opArticle = articleMapper.selectByAlias(article.getAlias());
			if (opArticle.isPresent() && !opArticle.get().getId().equals(article.getId())) {
				throw new LogicException("articleService.update.aliasExists", "别名已经存在");
			}
		}
		Article old = opOld.get();
		if (article.getStatus().equals(ArticleStatus.PUBLISHED)) {
			article.setPubDate(old.getPubDate());
			LocalDateTime now = LocalDateTime.now();
			if (article.getPubDate() == null || article.getPubDate().isAfter(now)) {
				article.setPubDate(now);
			}
		} else if (article.getStatus().equals(ArticleStatus.DRAFT)) {
			article.setPubDate(old.getPubDate());
		}

		processCategoriesAndTags(article);
		article.setLastModifyDate(LocalDateTime.now());
		articleMapper.update(article);

		if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
			makeArticlesIndexable(List.of(article));
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				if (old.getStatus().equals(ArticleStatus.PUBLISHED)) {
					try {
						articleIndexer.deleteDocument(old.getId());
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
				if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
					try {
						articleIndexer.updateDocument(article);
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} else if (ArticleStatus.SCHEDULED.equals(article.getStatus())) {
					scheduleManager.update();
				}
			}
		});
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteArticle(int id) {
		Optional<Article> opArticle = articleMapper.selectById(id);
		if (opArticle.isEmpty()) {
			return;
		}
		Article article = opArticle.get();
		articleCategoryMapper.deleteByArticle(id);
		articleTagMapper.deleteByArticle(id);
		commentMapper.deleteByModule(new CommentModule(COMMENT_MODULE_NAME, id));
		articleMapper.deleteById(id);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
					try {
						articleIndexer.deleteDocument(id);
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			}
		});

	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int saveArticle(Article article) {
		if (article.getAlias() != null) {
			articleMapper.selectByAlias(article.getAlias()).ifPresent(a -> {
				throw new LogicException("articleService.save.aliasExists", "别名已经存在");
			});
		}
		switch (article.getStatus()) {
		case DRAFT:
			article.setPubDate(null);
			break;
		case PUBLISHED:
			article.setPubDate(LocalDateTime.now());
			break;
		default:
			break;
		}

		articleMapper.insert(article);

		processCategoriesAndTags(article);
		if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
			makeArticlesIndexable(List.of(article));
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
					try {
						articleIndexer.addDocument(article);
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} else if (ArticleStatus.SCHEDULED.equals(article.getStatus())) {
					scheduleManager.update();
				}
			}
		});

		return article.getId();
	}

	@Transactional(readOnly = true)
	public Optional<Article> getArticle(String idOrAlias) {
		Optional<Article> opArticle;
		try {
			int id = Integer.parseInt(idOrAlias);
			opArticle = articleMapper.selectById(id);
		} catch (NumberFormatException e) {
			opArticle = articleMapper.selectByAlias(idOrAlias);
		}
		if (opArticle.isPresent()) {
			Article article = opArticle.get();
			if (!BlogContext.isAuthenticated() && !ArticleStatus.PUBLISHED.equals(article.getStatus())) {
				return Optional.empty();
			}
			SecurityChecker.check(article);
			processArticles(List.of(article), BlogContext.isAuthenticated());
			return Optional.of(article);
		}
		return opArticle;
	}

	@Transactional(readOnly = true)
	public Optional<Article> getArticleForEdit(int id) {
		return articleMapper.selectById(id);
	}

	@Transactional(readOnly = true)
	public List<ArticleTagStatistic> getArticleTagStatistic(String category) {
		Integer categoryId = null;
		if (!StringUtils.isEmptyOrWhitespace(category)) {
			categoryId = categoryCache.getAll().stream().filter(c -> c.getName().equals(category)).map(Category::getId)
					.findAny().orElse(null);
			if (categoryId == null) {
				return List.of();
			}
		}
		return articleTagMapper.selectCount(BlogContext.isAuthenticated(), categoryId);
	}

	@Transactional(readOnly = true)
	public List<ArticleCategoryStatistic> getArticleCategoryStatistic() {
		return articleCategoryMapper.selectCount(BlogContext.isAuthenticated());
	}

	@Transactional(readOnly = true)
	public ArticleStatistic getArticleStatistic() {
		boolean queryPrivate = BlogContext.isAuthenticated();
		ArticleStatistic stat = articleMapper.selectStatistic(queryPrivate);
		List<ArticleCategoryStatistic> categoryStatistics = articleCategoryMapper.selectCount(queryPrivate);
		List<ArticleStatusStatistic> statusStatistics = articleMapper.selectStatusStatistic(queryPrivate);
		stat.setCategoryStatistics(categoryStatistics);
		stat.setStatusStatistics(statusStatistics);
		return stat;
	}

	@Transactional(readOnly = true)
	public Optional<Article> prev(String idOrAlias, Set<String> categories, Set<String> tags) {
		return prevOrNext(idOrAlias, categories, tags, true);
	}

	@Transactional(readOnly = true)
	public Optional<Article> next(String idOrAlias, Set<String> categories, Set<String> tags) {
		return prevOrNext(idOrAlias, categories, tags, false);
	}

	private Optional<Article> prevOrNext(String idOrAlias, Set<String> categories, Set<String> tags, boolean prev) {
		Set<Integer> categorySet = null;
		Set<Integer> tagSet = null;

		if (!CollectionUtils.isEmpty(categories)) {
			categorySet = categories.stream()
					.map(name -> categoryCache.getAll().stream().filter(c -> c.getName().equals(name)).findAny())
					.filter(Optional::isPresent).map(Optional::get).map(Category::getId).collect(Collectors.toSet());
			if (categorySet.isEmpty()) {
				return Optional.empty();
			}
		}

		if (!CollectionUtils.isEmpty(tags)) {
			tagSet = tags.stream()
					.map(name -> tagCache.getAll().stream().filter(c -> c.getName().equals(name)).findAny())
					.filter(Optional::isPresent).map(Optional::get).map(Tag::getId).collect(Collectors.toSet());
			if (tagSet.isEmpty()) {
				return Optional.empty();
			}
		}

		Optional<Article> opArticle;
		try {
			int id = Integer.parseInt(idOrAlias);
			opArticle = articleMapper.selectById(id);
		} catch (NumberFormatException e) {
			opArticle = articleMapper.selectByAlias(idOrAlias);
		}
		if (opArticle.isPresent()) {
			Article article = opArticle.get();
			if (!ArticleStatus.PUBLISHED.equals(article.getStatus())) {
				return Optional.empty();
			}
			SecurityChecker.check(article);
			boolean queryPrivate = BlogContext.isAuthenticated();
			Optional<Article> op = prev ? articleMapper.selectPrev(article, categorySet, tagSet, queryPrivate)
					: articleMapper.selectNext(article, categorySet, tagSet, queryPrivate);
			if (op.isPresent()) {
				processArticles(List.of(op.get()), queryPrivate);
				op.get().setContent(null);
			}
			return op;
		}
		return opArticle;
	}

	@Transactional(readOnly = true)
	public PageResult<Article> queryArticle(ArticleQueryParam queryParam) {
		Integer categoryId = null, tagId = null;

		if (!StringUtils.isEmptyOrWhitespace(queryParam.getCategory())) {
			for (Category category : categoryCache.getAll()) {
				if (category.getName().equals(queryParam.getCategory())) {
					categoryId = category.getId();
					break;
				}
			}
			if (categoryId == null) {
				return new PageResult<>(queryParam, 0, List.of());
			}
		}

		if (!StringUtils.isEmptyOrWhitespace(queryParam.getTag())) {
			for (Tag tag : tagCache.getAll()) {
				if (tag.getName().equals(queryParam.getTag())) {
					tagId = tag.getId();
					break;
				}
			}
			if (tagId == null) {
				return new PageResult<>(queryParam, 0, List.of());
			}
		}

		HandledArticleQueryParam param = new HandledArticleQueryParam(queryParam, categoryId, tagId);

		if (!BlogContext.isAuthenticated()) {
			param.setQueryPrivate(false);
			param.setQueryPasswordProtected(false);
			param.setStatus(ArticleStatus.PUBLISHED);
		}

		String query = param.getQuery();
		PageResult<Article> articlePage;
		if (StringUtils.isEmptyOrWhitespace(query)) {
			int count = articleMapper.selectCount(param);
			if (count == 0) {
				articlePage = new PageResult<>(param, 0, List.of());
			} else {
				List<Article> articles = articleMapper.selectPage(param);
				articlePage = new PageResult<>(param, count, articles);
			}
		} else {
			PageResult<Integer> idPage;
			try {
				idPage = articleIndexer.query(param);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (idPage.getDatas().isEmpty()) {
				return new PageResult<>(param, idPage.getTotalRow(), List.of());
			}
			List<Article> articles = articleMapper.selectByIds(idPage.getDatas());
			Map<Integer, Article> sortHelper = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
			articles = idPage.getDatas().stream().map(sortHelper::get).filter(Objects::nonNull)
					.collect(Collectors.toList());
			articlePage = new PageResult<Article>(param, idPage.getTotalRow(), articles);
		}
		processArticles(articlePage.getDatas(), BlogContext.isAuthenticated());
		for (Article article : articlePage.getDatas()) {
			article.setContent(null);
		}
		return articlePage;
	}

	@Transactional(readOnly = true)
	public PageResult<ArticleArchive> queryArticleArchives(ArticleArchiveQueryParam param) {
		if (param.isQueryPrivate()) {
			param.setQueryPrivate(BlogContext.isAuthenticated());
		}

		int count = articleMapper.selectDaysCount(param);
		List<LocalDate> days = articleMapper.selectDays(param);
		int size = days.size();
		if (size == 0) {
			return new PageResult<>(param, count, Collections.emptyList());
		}
		LocalDateTime begin, end;
		if (size == 1) {
			LocalDate localDate = days.get(0);
			begin = localDate.atStartOfDay();
			end = localDate.plusDays(1).atStartOfDay();
		} else {
			LocalDate max = days.get(0);
			LocalDate min = days.get(size - 1);
			begin = min.atStartOfDay();
			end = max.plusDays(1).atStartOfDay();
		}

		ArticleQueryParam aqp = new ArticleQueryParam();
		aqp.setIgnoreLevel(true);
		aqp.setBegin(begin);
		aqp.setEnd(end);
		aqp.setQueryPrivate(param.isQueryPrivate());
		aqp.setStatus(ArticleStatus.PUBLISHED);
		aqp.setIgnorePaging(true);
		HandledArticleQueryParam haqp = new HandledArticleQueryParam(aqp, null, null);

		List<Article> articles = articleMapper.selectPage(haqp);
		processArticles(articles, BlogContext.isAuthenticated());
		for (Article article : articles) {
			article.setContent(null);
		}

		Map<LocalDate, List<Article>> map = articles.stream().collect(Collectors.groupingBy(article -> {
			return article.getPubDate().toLocalDate();
		}));

		List<ArticleArchive> archives = days.stream().map(d -> new ArticleArchive(d, map.get(d)))
				.collect(Collectors.toList());
		return new PageResult<>(param, count, archives);
	}

	// hits++
	@Transactional(propagation = Propagation.REQUIRED)
	public void hit(int id) {
		if (BlogContext.isAuthenticated()) {
			return;
		}
		Optional<Article> opArticle = articleMapper.selectById(id);
		if (opArticle.isEmpty()) {
			throw new ResourceNotFoundException("article.notExists", "文章不存在");
		}
		Article article = opArticle.get();
		SecurityChecker.check(article);
		articleMapper.increaseHits(id, 1);
	}

	private void processCategoriesAndTags(Article article) {
		articleCategoryMapper.deleteByArticle(article.getId());
		if (!CollectionUtils.isEmpty(article.getCategories())) {
			for (Category category : article.getCategories()) {
				if (categoryCache.get(category.getId()) == null) {
					throw new LogicException("articleService.category.notExists", "分类不存在");
				}
				articleCategoryMapper.insert(new ArticleCategory(article.getId(), category.getId()));
			}
		}
		articleTagMapper.deleteByArticle(article.getId());
		if (!CollectionUtils.isEmpty(article.getTags())) {
			for (Tag tag : article.getTags()) {
				if (tagCache.get(tag.getId()) == null) {
					throw new LogicException("articleService.tag.notExists", "标签不存在");
				}
				articleTagMapper.insert(new ArticleTag(article.getId(), tag.getId()));
			}
		}
	}

	private void makeArticlesIndexable(List<Article> articles) {
		processArticleContents(articles);
	}

	private void processArticleContents(List<Article> articles) {
		Map<Integer, String> markdownMap = new HashMap<>();
		for (Article article : articles) {
			if (!StringUtils.isEmptyOrWhitespace(article.getSummary())) {
				markdownMap.put(article.getId(), article.getSummary());
			}
			if (!StringUtils.isEmptyOrWhitespace(article.getContent())) {
				markdownMap.put(-article.getId(), article.getContent());
			}
		}
		if (markdownMap != null) {
			Map<Integer, String> htmlMap = markdown2Html.toHtmls(markdownMap);
			for (Article article : articles) {
				article.setSummary(htmlMap.get(article.getId()));
				article.setContent(htmlMap.get(-article.getId()));
			}
		}
	}

	private void processArticles(List<Article> articles, boolean authenticated) {
		if (articles.isEmpty()) {
			return;
		}
		if (!authenticated) {
			articles.stream().filter(SecurityChecker::locked).forEach(Article::clearProtected);
		}
		for (Article article : articles) {
			article.setCategories(article.getCategories().stream().map(Category::getId).map(categoryCache::get)
					.filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
			article.setTags(article.getTags().stream().map(Tag::getId).map(tagCache::get).filter(Objects::nonNull)
					.collect(Collectors.toCollection(LinkedHashSet::new)));
		}
		processArticleContents(articles);
		for (Article article : articles) {
			if (article.getContent() == null) {
				continue;
			}
			if (article.getFeatureImage() != null) {
				continue;
			}
			Optional<String> opFirstImage = JsoupUtils.getFirstImage(article.getContent());
			if (opFirstImage.isPresent()) {
				article.setFeatureImage(opFirstImage.get());
			}
		}
	}

	private final class ScheduleManager implements Closeable {
		private LocalDateTime start;

		private final TransactionTemplate writeTemplate;
		private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

		public ScheduleManager(PlatformTransactionManager transactionManager) {
			super();
			this.writeTemplate = new TransactionTemplate(transactionManager);
			this.writeTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			update();
			ses.scheduleAtFixedRate(this::publish, 5, 5, TimeUnit.SECONDS);
		}

		private void publish() {
			if (start == null) {
				return;
			}
			if (start.isAfter(LocalDateTime.now())) {
				return;
			} else {
				LocalDateTime startCopy = LocalDateTime.of(start.toLocalDate(), start.toLocalTime());
				writeTemplate.executeWithoutResult(status -> {
					List<Article> schedules = articleMapper.selectScheduled(start);
					if (schedules.isEmpty()) {
						return;
					}
					for (Article article : schedules) {
						article.setStatus(ArticleStatus.PUBLISHED);
						articleMapper.update(article);
					}
					makeArticlesIndexable(schedules);
					start = articleMapper.selectMinimumScheduleDate().orElse(null);
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

						@Override
						public void afterCompletion(int status) {
							if (status == TransactionSynchronization.STATUS_COMMITTED) {
								try {
									articleIndexer.addDocument(schedules.stream().toArray(Article[]::new));
								} catch (IOException e) {
									throw new RuntimeException(e.getMessage(), e);
								}
							} else {
								start = startCopy;
							}
						}

					});
				});
			}
		}

		public void update() {
			start = articleMapper.selectMinimumScheduleDate().orElse(null);
		}

		@Override
		public void close() throws IOException {
			if (!this.ses.isShutdown()) {
				this.ses.shutdownNow();
			}
		}
	}

	@EventListener(ContextClosedEvent.class)
	public void handleContextCloseEvent() throws IOException {
		this.scheduleManager.close();
	}

	@TransactionalEventListener(value = TagDeleteEvent.class, phase = TransactionPhase.BEFORE_COMMIT)
	public void handleTagDeleteEventBeforeCommit(TagDeleteEvent event) {
		articleTagMapper.deleteByTag(event.getTag().getId());
	}

	@TransactionalEventListener(value = CategoryDeleteEvent.class, phase = TransactionPhase.BEFORE_COMMIT)
	public void handleCategoryDeleteEventBeforeCommit(CategoryDeleteEvent event) {
		if (articleCategoryMapper.isArticleExists(event.getCategory().getId())) {
			throw new LogicException("categoryService.delete.articleExists", "分类下存在文章，无法删除");
		}
		articleCategoryMapper.deleteByCategory(event.getCategory().getId());
	}

	@Override
	public Article checkBeforeQuery(CommentModule module) {
		Article article = doCheck(module);
		Article rt = new Article();
		rt.setId(article.getId());
		rt.setAlias(article.getAlias());
		return rt;
	}

	@Override
	public void checkBeforeSave(Comment comment, CommentModule module) {
		Article article = doCheck(module);
		if (!BlogContext.isAuthenticated() && !article.getAllowComment()) {
			throw new LogicException("article.disableComment", "文章禁止评论");
		}
	}

	private Article doCheck(CommentModule module) {
		Article article = articleMapper.selectById(module.getId())
				.orElseThrow(() -> new ResourceNotFoundException("article.notExists", "文章不存在"));
		SecurityChecker.check(article);
		return article;
	}

	@Override
	public String getModuleName() {
		return COMMENT_MODULE_NAME;
	}
}
