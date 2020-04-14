package me.qyh.blog.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.ExpressionCacheKey;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheEntryValidityChecker;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.cache.TemplateCacheKey;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;

import me.qyh.blog.BlogContext;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.entity.Template;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CommentMapper;
import me.qyh.blog.mapper.TemplateMapper;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StreamUtils;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.TemplateQueryParam;
import me.qyh.blog.web.template.TemplateUtils;

/**
 * 模板管理器，用来注册页面
 * 
 * @author wwwqyhme
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TemplateService
		implements HandlerMapping, ITemplateResolver, CommentModuleHandler<Template>, ICacheManager {

	private final UrlPathHelper urlPathHelper = new UrlPathHelper();
	private final StampedLock lock = new StampedLock();
	private final PathMatcher pathMatcher = new AntPathMatcher();
	public static final String ROOT_TEMPLATE_KEY = TemplateService.class.getName() + ".root_template.request";
	public static final String TEMPLATE_NAME = "template";

	private final Path regPath = Paths.get(System.getProperty("user.home")).resolve("blog/defaultTemplatesReg");

	private final Map<String, Template> urlPatternMap = new HashMap<>();
	private final Map<String, Template> previewUrlPatternMap = new HashMap<>();

	private final Map<String, Template> fragmentMap = new HashMap<>();
	private final Map<String, Template> previewFragmentMap = new HashMap<>();

	private String previewIp;// current preview ip

	private final TemplateMapper templateMapper;
	private final TransactionTemplate readOnlyTemplate;
	private final TransactionTemplate writeTemplate;

	// default error template
	private static final Template error;
	// error page has an error
	private static final Template errorPageError;
	// unlock
	private static final Template unlock;

	public static String ERROR_PAGE_ERROR_TEMPLATE_NAME = "errorPageError";
	public static String UNLOCK_TEMPLATE_NAME = "unlock";

	private static final String ALL_ERROR_TEMPLATE_NAME = "error/*";
	private static final String COMMENT_MODULE_NAME = "template";
	private static final String ERROR_PATH = "/error";

	private int previewId;

	private final CommentMapper commentMapper;
	private final TemplateCache templateCache = new TemplateCache();

	static {
		String errorContent = readResourceToString(new ClassPathResource("defaultTemplates/error.html"));
		error = new Template();
		error.setName(ALL_ERROR_TEMPLATE_NAME);
		error.setContent(errorContent);
		error.setEnable(true);

		String errorPageErrorContent = readResourceToString(
				new ClassPathResource("defaultTemplates/errorPageError.html"));
		errorPageError = new Template();
		errorPageError.setName(ERROR_PAGE_ERROR_TEMPLATE_NAME);
		errorPageError.setContent(errorPageErrorContent);
		errorPageError.setEnable(true);

		String unlockContent = readResourceToString(new ClassPathResource("defaultTemplates/unlock.html"));
		unlock = new Template();
		unlock.setName(UNLOCK_TEMPLATE_NAME);
		unlock.setContent(unlockContent);
		unlock.setEnable(true);
	}

	public TemplateService(TemplateMapper templateMapper, PlatformTransactionManager transactionManager,
			CommentMapper commentMapper) {
		this.templateMapper = templateMapper;
		this.commentMapper = commentMapper;
		this.readOnlyTemplate = new TransactionTemplate(transactionManager);
		this.readOnlyTemplate.setReadOnly(true);
		this.writeTemplate = new TransactionTemplate(transactionManager);
		this.writeTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		registerAllDefaultTemplates();
		fragmentMap.put(unlock.getName(), unlock);
		fragmentMap.put(error.getName(), error);
		fragmentMap.put(errorPageError.getName(), errorPageError);
		registerAllEnabledTemplates();
	}

	/**
	 * 分页查询模板
	 * 
	 * @param param
	 * @return
	 */
	public PageResult<Template> queryTemplate(TemplateQueryParam param) {
		return readOnlyTemplate.execute(new TransactionCallback<PageResult<Template>>() {

			@Override
			public PageResult<Template> doInTransaction(TransactionStatus status) {
				int count = templateMapper.selectCount(param);
				if (count > 0) {
					List<Template> templates = templateMapper.selectPage(param);
					return new PageResult<Template>(param, count, templates);
				}
				return new PageResult<Template>(param, count, Collections.emptyList());
			}
		});
	}

	/**
	 * 注册一个模板
	 * 
	 * @param template
	 */
	public int registerTemplate(Template template) {
		checkReservedPattern(template);
		long stamp = lock.writeLock();
		try {
			return writeTemplate.execute(new TransactionCallback<Integer>() {

				@Override
				public Integer doInTransaction(TransactionStatus status) {
					disableEnable(template);
					template.setCreateTime(LocalDateTime.now());
					templateMapper.insert(template);
					if (template.getEnable()) {
						TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

							@Override
							public void afterCommit() {
								if (template.getPattern() != null) {
									previewUrlPatternMap.remove(template.getPattern());
									urlPatternMap.put(template.getPattern(), template);
								} else {
									templateCache.clearCacheFor(template.getName());
									previewFragmentMap.remove(template.getName());
									fragmentMap.put(template.getName(), template);
								}
							}
						});
					}
					return template.getId();
				}
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 更新一个模板
	 * 
	 * @param template
	 */
	public void updateTemplate(Template template) {
		checkReservedPattern(template);
		long stamp = lock.writeLock();
		try {
			writeTemplate.executeWithoutResult(status -> {
				// find old template
				Template old = templateMapper.selectById(template.getId()).orElseThrow(
						() -> new ResourceNotFoundException("templateService.update.notExists", "要更新的模板不存在"));
				disableEnable(template);
				template.setModifyTime(LocalDateTime.now());
				templateMapper.update(template);

				if (old.getEnable() || template.getEnable()) {
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

						@Override
						public void afterCommit() {
							if (old.getEnable()) {
								if (old.getPattern() != null) {
									urlPatternMap.remove(old.getPattern());
								} else {
									removeFragment(old.getName());
								}
							}
							if (template.getEnable()) {
								if (template.getPattern() != null) {
									previewUrlPatternMap.remove(template.getPattern());
									urlPatternMap.put(template.getPattern(), template);
								} else {
									templateCache.clearCacheFor(template.getName());
									previewFragmentMap.remove(template.getName());
									fragmentMap.put(template.getName(), template);
								}
							}
						}
					});
				}
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 注册一个预览模板
	 * 
	 * @param template
	 */
	public void registerPreviewTemplate(Template template) {
		checkReservedPattern(template);
		long stamp = lock.writeLock();
		try {
			previewId++;
			template.setId(previewId);
			if (template.getPattern() != null) {
				previewUrlPatternMap.put(template.getPattern(), template);
			} else {
				templateCache.clearCacheFor(template.getName());
				previewFragmentMap.put(template.getName(), template);
			}
			previewIp = BlogContext.getIP().orElse(null);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 获取一个模板，如果模板处于启用状态，并且当前IP是预览IP，那么模板内容会被替换为预览模板内容(如果存在对应名称或者路径的模板)
	 * 
	 * @param id
	 */
	public Optional<Template> getTemplate(int id) {
		return readOnlyTemplate.execute(new TransactionCallback<Optional<Template>>() {

			@Override
			public Optional<Template> doInTransaction(TransactionStatus status) {
				Optional<Template> opTemplate = templateMapper.selectById(id);
				if (opTemplate.isPresent() && opTemplate.get().getEnable() && previewIp != null
						&& previewIp.equals(BlogContext.getIP().orElse(null))) {
					long stamp = lock.readLock();
					try {
						Template template = opTemplate.get();
						Optional.ofNullable(template.getName() != null ? previewFragmentMap.get(template.getName())
								: previewUrlPatternMap.get(template.getPattern())).map(Template::getContent)
								.ifPresent(template::setContent);
					} finally {
						lock.unlockRead(stamp);
					}
				}
				return opTemplate;
			}

		});
	}

	/**
	 * 删除一个模板
	 * 
	 * @param id
	 */
	public void deleteTemplate(int id) {
		long stamp = lock.writeLock();
		try {
			writeTemplate.executeWithoutResult(status -> {
				Optional<Template> opOld = templateMapper.selectById(id);
				if (opOld.isPresent()) {
					Template old = opOld.get();
					templateMapper.deleteById(id);
					commentMapper.deleteByModule(new CommentModule(COMMENT_MODULE_NAME, id));
					if (old.getEnable()) {
						TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

							@Override
							public void afterCommit() {
								if (old.getPattern() != null) {
									previewUrlPatternMap.remove(old.getPattern());
									urlPatternMap.remove(old.getPattern());
								} else {
									previewFragmentMap.remove(old.getName());
									removeFragment(old.getName());
								}
							}
						});
					}
				}
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 移除所有的预览模板
	 */
	public void clearPreviewTemplates() {
		long stamp = lock.writeLock();
		try {
			previewUrlPatternMap.clear();
			previewFragmentMap.clear();
			previewIp = null;
			previewId = 0;
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 获取所有的预览模板
	 * 
	 * @return
	 */
	public List<Template> getPreviewTemplates() {
		long stamp = lock.readLock();
		try {
			List<Template> templates = new ArrayList<>();
			templates.addAll(previewUrlPatternMap.values());
			templates.addAll(previewFragmentMap.values());
			return templates;
		} finally {
			lock.unlockRead(stamp);
		}
	}

	/**
	 * 删除某一个预览模板
	 * 
	 * @param id
	 */
	public void deletePreviewTemplate(int id) {
		long stamp = lock.writeLock();
		try {
			previewFragmentMap.entrySet().removeIf(e -> e.getValue().getId().intValue() == id);
			previewUrlPatternMap.entrySet().removeIf(e -> e.getValue().getId().intValue() == id);
			if (previewFragmentMap.isEmpty() && previewUrlPatternMap.isEmpty()) {
				previewIp = null;
				previewId = 0;
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * 获取默认模板
	 * 
	 * @return
	 */
	public List<Template> getDefaultTemplates() {
		String navTemplate = readResourceToString(new ClassPathResource("defaultTemplates/nav.html"));
		String indexTemplate = readResourceToString(new ClassPathResource("defaultTemplates/index.html"));
		String articleTemplate = readResourceToString(new ClassPathResource("defaultTemplates/article.html"));
		String momentsTemplate = readResourceToString(new ClassPathResource("defaultTemplates/moments.html"));
		String momentTemplate = readResourceToString(new ClassPathResource("defaultTemplates/moment.html"));
		String archiveTemplate = readResourceToString(new ClassPathResource("defaultTemplates/archive.html"));
		Template nav = new Template("nav", null, navTemplate);
		Template index = new Template(null, "/", indexTemplate);
		Template article = new Template(null, "/articles/{idOrAlias}", articleTemplate);
		Template moments = new Template(null, "/moments", momentsTemplate);
		Template moment = new Template(null, "/moments/{id}", momentTemplate);
		Template archive = new Template(null, "/archive", archiveTemplate);
		List<Template> defaultTemplates = List.of(nav, index, article, moments, moment, archive);
		defaultTemplates.forEach(t -> {
			t.setEnable(true);
			t.setAllowComment(false);
		});
		return defaultTemplates;
	}

	/**
	 * 将预览模板保存到模板中
	 * 
	 * <p>
	 * <b>如果对应路径|名称的模板不存在或者未启用，将会插入一个新的模板</b>
	 * </p>
	 */
	public void mergePreviewTemplates() {
		long stamp = lock.writeLock();
		try {
			if (previewFragmentMap.isEmpty() && previewUrlPatternMap.isEmpty()) {
				return;
			}

			writeTemplate.executeWithoutResult(status -> {

				List<Template> merges = new ArrayList<>();

				Stream.concat(previewFragmentMap.values().stream(), previewUrlPatternMap.values().stream())
						.forEach(template -> {
							String pattern = template.getPattern();
							String name = template.getName();
							Optional<Template> dbOptional;
							if (pattern != null) {
								dbOptional = templateMapper.selectEnabledByPattern(pattern);
							} else {
								dbOptional = templateMapper.selectEnabledByName(name);
							}
							if (dbOptional.isEmpty()) {
								Template newTemplate = new Template();
								newTemplate.setName(name);
								newTemplate.setPattern(pattern);
								newTemplate.setAllowComment(false);
								newTemplate.setContent(template.getContent());
								newTemplate.setCreateTime(LocalDateTime.now());
								newTemplate.setEnable(true);
								templateMapper.insert(newTemplate);
								merges.add(newTemplate);
							} else {
								Template db = dbOptional.get();
								db.setContent(template.getContent());
								db.setModifyTime(LocalDateTime.now());
								templateMapper.update(db);
								merges.add(db);
							}
						});

				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {

						for (Template template : merges) {
							if (template.getPattern() != null) {
								urlPatternMap.remove(template.getPattern());
								urlPatternMap.put(template.getPattern(), template);
							} else {
								templateCache.clearCacheFor(template.getName());
								fragmentMap.remove(template.getName());
								fragmentMap.put(template.getName(), template);
							}
						}

						previewFragmentMap.clear();
						previewUrlPatternMap.clear();
						previewIp = null;
						previewId = 0;
					}
				});
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	public boolean isPreviewRequest(HttpServletRequest request) {
		return this.previewIp != null && Objects.equals(this.previewIp, BlogContext.getIP().orElse(null));
	}

	@Override
	public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {

		// only support get method
		if (!"get".equalsIgnoreCase(request.getMethod())) {
			return null;
		}

		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);

		if (lookupPath.equalsIgnoreCase(ERROR_PATH)) {
			return null;// use system error controller
		}

		request.setAttribute(LOOKUP_PATH, lookupPath);

		long stamp = lock.tryOptimisticRead();
		try {
			retryHoldingLock: for (;; stamp = lock.readLock()) {
				if (stamp == 0L)
					continue retryHoldingLock;
				HandlerExecutionChain chain = findChain(lookupPath, request);
				if (!lock.validate(stamp))
					continue retryHoldingLock;
				return chain;
			}
		} finally {
			if (StampedLock.isReadLockStamp(stamp)) {
				lock.unlockRead(stamp);
			}
		}
	}

	private HandlerExecutionChain findChain(String lookupPath, HttpServletRequest request) {
		// first lookup best preview match
		Template bestPreview = null;
		boolean preview = TemplateUtils.isPreviewRequest(request);
		if (preview) {
			bestPreview = lookupTemplate(lookupPath, previewUrlPatternMap);
		}
		// then lookup best match
		Template best = lookupTemplate(lookupPath, urlPatternMap);

		Template finalTemplate = null;
		if (bestPreview != null && best != null) {
			// compare bestPreview & best
			Comparator<String> comparator = pathMatcher.getPatternComparator(lookupPath);
			int compare = comparator.compare(bestPreview.getPattern(), best.getPattern());
			if (compare == 0) {
				finalTemplate = bestPreview;
			} else if (compare < 0) {
				finalTemplate = best;
			}
		} else {
			finalTemplate = bestPreview == null ? best : bestPreview;
		}

		if (finalTemplate != null) {
			Map<String, String> uriVariables = pathMatcher.extractUriTemplateVariables(finalTemplate.getPattern(),
					lookupPath);
			Map<String, String> decodedUriVariables = urlPathHelper.decodePathVariables(request, uriVariables);
			request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decodedUriVariables);
			request.setAttribute(ROOT_TEMPLATE_KEY, new Template(finalTemplate));
			return new HandlerExecutionChain(TEMPLATE_NAME);
		} else {
			// remove request attributes
			request.removeAttribute(ROOT_TEMPLATE_KEY);
			request.removeAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		}

		return null;
	}

	private Template lookupTemplate(String lookupPath, Map<String, Template> urlPatternMap) {
		if (urlPatternMap.isEmpty()) {
			return null;
		}
		// first lookup best match
		Template best = urlPatternMap.get(lookupPath);
		if (best == null) {
			List<String> patterns = new ArrayList<>();
			// iterator all template
			for (Map.Entry<String, Template> it : urlPatternMap.entrySet()) {
				String pattern = it.getKey();
				String match = getMatchingPattern(pattern, lookupPath);
				if (match != null) {
					patterns.add(pattern);
				}
			}
			int size = patterns.size();
			Comparator<String> comparator = pathMatcher.getPatternComparator(lookupPath);
			if (size > 1) {
				patterns.sort(comparator);
			}

			if (size > 0) {
				String first = patterns.get(0);
				if (size > 1) {
					String second = patterns.get(1);

					if (comparator.compare(first, second) == 0) {
						Template _first = urlPatternMap.get(first);
						Template _second = urlPatternMap.get(second);
						// need to compare template'id
						if (_first.getId() > _second.getId()) {
							best = _first;
						} else {
							best = _second;
						}
					} else {
						best = urlPatternMap.get(first);
					}
				} else {
					best = urlPatternMap.get(first);
				}
			}
		}
		return best;
	}

	private String getMatchingPattern(String pattern, String lookupPath) {
		if (pattern.equals(lookupPath)) {
			return pattern;
		}
		if (pathMatcher.match(pattern, lookupPath)) {
			return pattern;
		}
		return null;
	}

	@Override
	public String getName() {
		return "blog template resolver";
	}

	@Override
	public Integer getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate, String template,
			Map<String, Object> templateResolutionAttributes) {

		Template rootTemplate = null;
		// find root template in request
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		int scope = RequestAttributes.SCOPE_REQUEST;
		rootTemplate = (Template) ra.getAttribute(ROOT_TEMPLATE_KEY, scope);
		boolean previewRequest = TemplateUtils.isPreviewRequest(((ServletRequestAttributes) ra).getRequest());

		if (ownerTemplate == null && rootTemplate != null && template.equals(TEMPLATE_NAME)) {
			return new TemplateResolution(new TemplateResource(rootTemplate), TemplateMode.HTML,
					// we cached template in service
					// do not cache again
					// though they are different cached value
					NonCacheableCacheEntryValidity.INSTANCE);
		}
		long stamp = lock.tryOptimisticRead();
		try {
			retryHoldingLock: for (;; stamp = lock.readLock()) {
				if (stamp == 0L)
					continue retryHoldingLock;
				Template lookupResult = findFragment(template, previewRequest);
				if (!lock.validate(stamp))
					continue retryHoldingLock;
				return lookupResult == null ? null
						: new TemplateResolution(new TemplateResource(new Template(lookupResult)), TemplateMode.HTML,
								NonCacheableCacheEntryValidity.INSTANCE);
			}
		} finally {
			if (StampedLock.isReadLockStamp(stamp)) {
				lock.unlockRead(stamp);
			}
		}
	}

	private Template findFragment(String templateName, boolean previewRequest) {
		Template template = null;
		if (previewRequest) {
			template = previewFragmentMap.get(templateName);
		}
		if (template == null) {
			template = fragmentMap.get(templateName);
		}
		if (template == null && StringUtils.startsWithIgnoreCase(templateName, "error/")
				&& !templateName.toLowerCase().equals(ALL_ERROR_TEMPLATE_NAME)) {
			return findFragment(ALL_ERROR_TEMPLATE_NAME, previewRequest);
		}
		return template;
	}

	private void disableEnable(Template template) {
		String pattern = template.getPattern();
		// no need to check if disable
		if (!template.getEnable()) {
			return;
		}
		Template disable = null;
		if (pattern != null) {
			Optional<Template> opDb = templateMapper.selectEnabledByPattern(pattern);
			if (opDb.isPresent()) {
				disable = opDb.get();
			}
		} else {
			String alias = template.getName();
			// find Template by alias
			Optional<Template> opDb = templateMapper.selectEnabledByName(alias);
			if (opDb.isPresent()) {
				disable = opDb.get();
			}
		}
		if (disable != null) {
			disable.setEnable(false);
			templateMapper.update(disable);
		}
	}

	private void removeFragment(String alias) {
		fragmentMap.remove(alias);
		if (alias.equals(ALL_ERROR_TEMPLATE_NAME)) {
			fragmentMap.put(ALL_ERROR_TEMPLATE_NAME, error);
		}
		if (alias.equals(ERROR_PAGE_ERROR_TEMPLATE_NAME)) {
			fragmentMap.put(ERROR_PAGE_ERROR_TEMPLATE_NAME, errorPageError);
		}
		if (alias.equals(UNLOCK_TEMPLATE_NAME)) {
			fragmentMap.put(UNLOCK_TEMPLATE_NAME, unlock);
		}
	}

	private void registerAllDefaultTemplates() {
		if (Files.exists(regPath)) {
			return;
		}
		List<Template> defaultTemplates = getDefaultTemplates();
		writeTemplate.executeWithoutResult(status -> {
			for (Template template : defaultTemplates) {
				if ((template.getName() != null && templateMapper.selectEnabledByName(template.getName()).isEmpty())
						|| (template.getPattern() != null
								&& templateMapper.selectEnabledByPattern(template.getPattern()).isEmpty())) {
					template.setCreateTime(LocalDateTime.now());
					templateMapper.insert(template);
				}
			}

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				@Override
				public void afterCommit() {
					FileUtils.createFile(regPath);
				}
			});
		});
	}

	private void registerAllEnabledTemplates() {
		templateMapper.selectEnabled().forEach(template -> {
			if (template.getPattern() != null) {
				urlPatternMap.put(template.getPattern(), template);
			} else {
				fragmentMap.put(template.getName(), template);
			}
		});
	}

	private static String readResourceToString(Resource resource) {
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.toString(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public final class TemplateResource implements ITemplateResource {

		private final Template template;

		@Override
		public String getDescription() {
			return null;
		}

		private TemplateResource(Template template) {
			super();
			this.template = template;
		}

		@Override
		public String getBaseName() {
			return template.getName();
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public Reader reader() throws IOException {
			return new StringReader(template.getContent());
		}

		@Override
		public ITemplateResource relative(String relativeLocation) {
			throw new TemplateInputException("not support relative template");
		}

		public Template getTemplate() {
			return template;
		}
	}

	@Override
	public Template checkBeforeQuery(CommentModule module) {
		Template template = doCheck(module);
		Template tpl = new Template();
		tpl.setPattern(template.getPattern());
		return tpl;
	}

	@Override
	public void checkBeforeSave(Comment comment, CommentModule module) {
		Template template = doCheck(module);
		if (!BlogContext.isAuthenticated() && !template.getAllowComment()) {
			throw new LogicException("template.disableComment", "页面禁止评论");
		}
	}

	private Template doCheck(CommentModule module) {
		Optional<Template> opTemplate = templateMapper.selectById(module.getId()).filter(Template::getEnable);

		if (opTemplate.isEmpty()) {
			throw new ResourceNotFoundException("template.notExists", "页面不存在");
		}

		return opTemplate.get();
	}

	@Override
	public String getModuleName() {
		return COMMENT_MODULE_NAME;
	}

	private void checkReservedPattern(Template template) {
		if (ERROR_PATH.equals(template.getPattern())) {
			throw new LogicException("templateService.reservedPattern", "不能覆盖" + ERROR_PATH + "路径",
					template.getPattern());
		}
	}

	@Override
	public ICache<TemplateCacheKey, TemplateModel> getTemplateCache() {
		return templateCache;
	}

	@Override
	public ICache<ExpressionCacheKey, Object> getExpressionCache() {
		return null;
	}

	@Override
	public <K, V> ICache<K, V> getSpecificCache(String name) {
		return null;
	}

	@Override
	public List<String> getAllSpecificCacheNames() {
		return null;
	}

	@Override
	public void clearAllCaches() {
		templateCache.clear();
	}

	private final class TemplateCache implements ICache<TemplateCacheKey, TemplateModel> {

		private final ConcurrentHashMap<TemplateCacheKey, SoftReference<TemplateModel>> cache = new ConcurrentHashMap<>();

		@Override
		public void put(TemplateCacheKey key, TemplateModel value) {
			cache.put(key, new SoftReference<>(value));
		}

		@Override
		public TemplateModel get(TemplateCacheKey key) {
			SoftReference<TemplateModel> ref = cache.get(key);
			if (ref != null) {
				return ref.get();
			}
			return null;
		}

		@Override
		public TemplateModel get(TemplateCacheKey key,
				ICacheEntryValidityChecker<? super TemplateCacheKey, ? super TemplateModel> validityChecker) {
			return get(key);
		}

		@Override
		public void clear() {
			cache.clear();
		}

		@Override
		public void clearKey(TemplateCacheKey key) {
			cache.remove(key);
		}

		@Override
		public Set<TemplateCacheKey> keySet() {
			return cache.keySet();
		}

		public void clearCacheFor(String name) {
			cache.keySet().removeIf(key -> {
				return key.getTemplate().equals(name)
						|| (key.getOwnerTemplate() != null && key.getOwnerTemplate().equals(name));
			});
		}

	}

}
