package me.qyh.blog.template.service.impl;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.reflect.TypeToken;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.SpaceDao;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.event.SpaceDelEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.DataTagProcessorRegistry;
import me.qyh.blog.core.plugin.TemplateRegistry;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.impl.EmptyCommentServer;
import me.qyh.blog.core.service.impl.Transactions;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.template.PatternAlreadyExistsException;
import me.qyh.blog.template.PreviewTemplate;
import me.qyh.blog.template.SystemTemplate;
import me.qyh.blog.template.Template;
import me.qyh.blog.template.TemplateMapping;
import me.qyh.blog.template.TemplateMapping.PreviewTemplateMapping;
import me.qyh.blog.template.dao.FragmentDao;
import me.qyh.blog.template.dao.HistoryTemplateDao;
import me.qyh.blog.template.dao.PageDao;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.entity.HistoryTemplate;
import me.qyh.blog.template.entity.HistoryTemplate.HistoryTemplateType;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.event.PageCreateEvent;
import me.qyh.blog.template.event.PageDelEvent;
import me.qyh.blog.template.event.PageUpdateEvent;
import me.qyh.blog.template.event.TemplateEvitEvent;
import me.qyh.blog.template.render.data.DataTagProcessor;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.vo.DataBind;
import me.qyh.blog.template.vo.DataTag;
import me.qyh.blog.template.vo.DataTagProcessorBean;
import me.qyh.blog.template.vo.ExportPage;
import me.qyh.blog.template.vo.ExportPages;
import me.qyh.blog.template.vo.FragmentQueryParam;
import me.qyh.blog.template.vo.ImportRecord;
import me.qyh.blog.template.vo.ImportRecord.ImportType;
import me.qyh.blog.template.vo.PageStatistics;
import me.qyh.blog.template.vo.PreviewImport;
import me.qyh.blog.template.vo.TemplatePageQueryParam;

/**
 * 模板服务类
 * <p>
 * 这个类所有对模板写操作的方法都是加锁的，因此在首次加载模板的时候效率很低
 * </p>
 * <p>
 * <b>这个类必须在Web环境中注册</b>
 * </p>
 * 
 * @author mhlx
 *
 */
public class TemplateServiceImpl implements TemplateService, ApplicationEventPublisherAware, InitializingBean,
		DataTagProcessorRegistry, TemplateRegistry {

	@Autowired
	private PageDao pageDao;
	@Autowired
	private FragmentDao fragmentDao;
	@Autowired
	private HistoryTemplateDao historyTemplateDao;
	@Autowired
	private SpaceDao spaceDao;
	@Autowired
	private ConfigServer configServer;
	@Autowired(required = false)
	private CommentServer commentServer;
	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	@Autowired
	private TemplateMapping templateMapping;

	private ApplicationEventPublisher applicationEventPublisher;

	private List<DataTagProcessor<?>> processors = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateServiceImpl.class);

	/**
	 * 系统默认模板片段
	 */
	private final List<Fragment> fragments = new ArrayList<>();

	private Map<String, SystemTemplate> defaultTemplates;

	private final List<TemplateProcessor> templateProcessors = new ArrayList<>();

	private String previewIp;

	private final List<Fragment> previewFragments = new ArrayList<>();

	private static final Path DATA_CONFIG = FileUtils.HOME_DIR.resolve("blog/data_config.json");

	static {
		if (!FileUtils.exists(DATA_CONFIG)) {
			FileUtils.createFile(DATA_CONFIG);
		}
	}

	@Override
	public synchronized Fragment insertFragment(Fragment fragment) throws LogicException {
		return Transactions.executeInTransaction(platformTransactionManager, status -> {
			Space space = fragment.getSpace();
			if (space != null) {
				fragment.setSpace(getRequiredSpace(space.getId()));
			}
			Fragment db;
			if (fragment.isGlobal()) {
				db = fragmentDao.selectGlobalByName(fragment.getName());
			} else {
				db = fragmentDao.selectBySpaceAndName(fragment.getSpace(), fragment.getName());
			}
			boolean nameExists = db != null;
			if (nameExists) {

				if (db.isDel()) {
					fragmentDao.deleteById(db.getId());
				} else {
					throw new LogicException("fragment.user.nameExists", "模板片段名:" + fragment.getName() + "已经存在",
							fragment.getName());
				}

			}

			fragment.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
			fragmentDao.insert(fragment);
			if (fragment.isEnable()) {
				evitFragmentCache(fragment.getName());
			}
			return fragment;
		});
	}

	@Override
	public synchronized void deleteFragment(Integer id) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			Fragment fragment = getRequiredFragment(id, ResourceNotFoundException::new);

			/**
			 * 
			 * @since 6.3
			 */
			fragment.setTpl("");
			fragment.setDel(true);
			fragment.setCallable(false);
			fragment.setCreateDate(Timestamp.valueOf(Times.now()));
			fragmentDao.update(fragment);
			historyTemplateDao.deleteByTemplate(fragment.getId(), HistoryTemplateType.FRAGMENT);

			evitFragmentCache(fragment.getName());
		});
	}

	@Override
	public synchronized Fragment updateFragment(Fragment fragment) throws LogicException {
		if (fragment.isGlobal()) {
			fragment.setSpace(null);
		}
		return Transactions.executeInTransaction(platformTransactionManager, status -> {
			Space space = fragment.getSpace();
			if (space != null) {
				fragment.setSpace(getRequiredSpace(space.getId()));
			}
			Fragment old = getRequiredFragment(fragment.getId(), ResourceNotFoundException::new);
			Fragment db;
			// 查找当前数据库是否存在同名
			if (fragment.isGlobal()) {
				db = fragmentDao.selectGlobalByName(fragment.getName());
			} else {
				db = fragmentDao.selectBySpaceAndName(fragment.getSpace(), fragment.getName());
			}
			boolean nameExists = db != null && !db.getId().equals(fragment.getId());
			if (nameExists) {
				if (db.isDel()) {
					fragmentDao.deleteById(db.getId());
				} else {
					throw new LogicException("fragment.user.nameExists", "模板片段名:" + fragment.getName() + "已经存在",
							fragment.getName());
				}
			}
			fragmentDao.update(fragment);

			if (old.getName().equals(fragment.getName())) {
				evitFragmentCache(old.getName());
			} else {
				evitFragmentCache(old.getName(), fragment.getName());
			}
			return fragment;
		});
	}

	@Override
	public PageResult<Fragment> queryFragment(FragmentQueryParam param) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			param.setPageSize(configServer.getGlobalConfig().getFragmentPageSize());
			int count = fragmentDao.selectCount(param);
			List<Fragment> datas = fragmentDao.selectPage(param);
			return new PageResult<>(param, count, datas);
		});
	}

	@Override
	public Optional<Fragment> queryFragment(Integer id) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			Fragment fragment = fragmentDao.selectById(id);
			if (fragment != null && fragment.isDel()) {
				return Optional.empty();
			}
			return Optional.ofNullable(fragment);
		});
	}

	@Override
	public Optional<Page> queryPage(Integer id) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			return Optional.ofNullable(pageDao.selectById(id));
		});
	}

	@Override
	public PageResult<Page> queryPage(TemplatePageQueryParam param) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			param.setPageSize(configServer.getGlobalConfig().getPagePageSize());
			int count = pageDao.selectCount(param);
			List<Page> datas = pageDao.selectPage(param);
			return new PageResult<>(param, count, datas);
		});
	}

	@Override
	public synchronized void deletePage(Integer id) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			Page db = getRequiredPage(id, ResourceNotFoundException::new);
			historyTemplateDao.deleteByTemplate(db.getId(), HistoryTemplateType.PAGE);
			pageDao.deleteById(id);
			commentServer.deleteComments(COMMENT_MODULE_NAME, id);
			String templateName = db.getTemplateName();
			evitPageCache(templateName);
			this.applicationEventPublisher.publishEvent(new PageDelEvent(this, List.of(db)));
			if (db.isEnable()) {
				new PageRequestMappingRegisterHelper().unregisterPage(db);
			}
		});
	}

	@Override
	public List<DataTagProcessorBean> queryDataTags() {
		return processors.stream().map(DataTagProcessorBean::new).collect(Collectors.toList());
	}

	@Override
	public synchronized Page createPage(Page page) throws LogicException {
		return Transactions.executeInTransaction(platformTransactionManager, status -> {
			PageRequestMappingRegisterHelper helper = new PageRequestMappingRegisterHelper();
			Space space = page.getSpace();
			if (space != null) {
				page.setSpace(getRequiredSpace(space.getId()));
			}

			String alias = page.getAlias();
			// 检查
			Page aliasPage = pageDao.selectBySpaceAndAlias(page.getSpace(), alias, page.isSpaceGlobal());
			if (aliasPage != null) {
				throw new LogicException("page.user.aliasExists", "别名" + alias + "已经存在", alias);
			}
			page.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
			pageDao.insert(page);

			evitPageCache(page);
			if (page.isEnable()) {
				// 注册现在的页面
				helper.registerPage(page);
			}

			this.applicationEventPublisher.publishEvent(new PageCreateEvent(this, page));
			return page;
		});
	}

	@Override
	public synchronized Page updatePage(Page page) throws LogicException {
		return Transactions.executeInTransaction(platformTransactionManager, status -> {
			final PageRequestMappingRegisterHelper helper = new PageRequestMappingRegisterHelper();
			Space space = page.getSpace();
			if (space != null) {
				page.setSpace(getRequiredSpace(space.getId()));
			}
			Page db = getRequiredPage(page.getId(), ResourceNotFoundException::new);
			String alias = page.getAlias();
			// 检查
			Page aliasPage = pageDao.selectBySpaceAndAlias(page.getSpace(), alias, page.isSpaceGlobal());
			if (aliasPage != null && !aliasPage.getId().equals(page.getId())) {
				throw new LogicException("page.user.aliasExists", "别名" + alias + "已经存在", alias);
			}
			pageDao.update(page);

			evitPageCache(db);

			if (db.isEnable()) {
				// 解除以前的mapping
				helper.unregisterPage(db);
			}
			if (page.isEnable()) {
				// 注册现在的页面
				helper.registerPage(page);
			}

			this.applicationEventPublisher.publishEvent(new PageUpdateEvent(this, db, page));
			return page;
		});
	}

	@Override
	public Optional<DataBind> queryData(DataTag dataTag, boolean onlyCallable) {
		Optional<DataTagProcessor<?>> processor = processors.stream()
				.filter(pro -> pro.getDataName().equals(dataTag.getName()) || pro.getName().equals(dataTag.getName()))
				.findAny();
		if (onlyCallable) {
			processor = processor.filter(DataTagProcessor::isCallable);
		}
		return processor.map(dataTagProcessor -> dataTagProcessor.getData(dataTag.getAttrs()));
	}

	@Override
	public Optional<Template> queryTemplate(String templateName) {
		if (!Template.isTemplate(templateName)) {
			return Optional.empty();
		}
		Optional<Template> op = getTemplateProcessor(templateName)
				.map(processor -> Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
					return processor.getTemplate(templateName);
				}));
		if (PreviewTemplate.isPreviewTemplate(templateName)) {
			return op.map(PreviewTemplate::new);
		}
		return op;
	}

	@Override
	public List<ExportPage> exportPage(Integer spaceId) throws LogicException {
		DefaultTransactionDefinition td = new DefaultTransactionDefinition();
		td.setReadOnly(true);
		TransactionStatus status = platformTransactionManager.getTransaction(td);
		try {
			Space space = spaceId == null ? null : getRequiredSpace(spaceId);
			List<ExportPage> exportPages = new ArrayList<>();
			for (Page page : pageDao.selectBySpace(space)) {
				exportPages.add(export(page));
			}
			return exportPages;
		} catch (LogicException | RuntimeException | Error e) {
			status.setRollbackOnly();
			throw e;
		} finally {
			platformTransactionManager.commit(status);
		}
	}

	@Override
	public synchronized void compareTemplate(String templateName, Template template, Consumer<Boolean> consumer) {
		Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			Optional<Template> current = queryTemplate(templateName);
			boolean equalsTo = current.isPresent() && template != null && current.get().equalsTo(template);
			consumer.accept(equalsTo);
		});
	}

	@Override
	public synchronized List<ImportRecord> importPage(ExportPages exportPages) {
		List<ExportPage> exportPageList = exportPages.getPages();
		if (CollectionUtils.isEmpty(exportPageList)) {
			return new ArrayList<>();
		}
		// 如果导入的空间不存在，直接返回
		Space space;
		try {
			space = exportPages.getSpaceId() == null ? null : getRequiredSpace(exportPages.getSpaceId());
		} catch (LogicException e) {
			List<ImportRecord> list = new ArrayList<>();
			list.add(new ImportRecord(false, e.getLogicMessage()));
			return list;
		}
		// 开启一个新的串行化事务
		DefaultTransactionDefinition td = new DefaultTransactionDefinition();
		td.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		td.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus ts = platformTransactionManager.getTransaction(td);
		List<ImportRecord> records = new ArrayList<>();
		// 设置一个新的页面mapping辅助类
		// 此时锁住RequestMappingHandlerMapping
		// 事务结束后自动解锁
		PageRequestMappingRegisterHelper helper = new PageRequestMappingRegisterHelper();
		try {
			// 用于导入结束后清空缓存
			Set<String> pageEvitKeySet = new HashSet<>();
			Set<String> fragmentEvitKeySet = new HashSet<>();

			/**
			 * 非默认空间不能拥有space global页面
			 * 
			 * @since 6.1
			 */
			Predicate<Page> filter = space == null ? page -> true : page -> !page.isSpaceGlobal();

			// 从导入页面中获取页面
			List<Page> pages = exportPageList.stream().map(ExportPage::getPage).filter(filter)
					.collect(Collectors.toList());
			// 从导入页面中获取fragments，按照name去重
			List<Fragment> fragments = exportPageList.stream().flatMap(ep -> ep.getFragments().stream()).distinct()
					.collect(Collectors.toList());

			for (Page page : pages) {
				// 设置空间，用于获取templateName
				page.setSpace(space);
				String templateName = page.getTemplateName();
				// 利用templateName查询当前是否已经存在页面
				Optional<Page> optional = queryPageWithTemplateName(templateName);
				// 如果不存在，插入一个自定义页面
				if (!optional.isPresent()) {
					page.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
					page.setDescription("");
					page.setAllowComment(false);

					pageDao.insert(page);

					if (page.isEnable()) {
						// 尝试注册mapping，如果此时存在了其他该路径的mapping(PathTemplate
						// mapping)那么无法注册成功
						try {
							helper.registerPage(page);
						} catch (LogicException ex) {
							records.add(new ImportRecord(false, ex.getLogicMessage()));
							ts.setRollbackOnly();
							return records;
						}
					}

					records.add(new ImportRecord(ImportType.NEW, new Message("import.insert.page.success",
							"插入页面" + page.getName() + "[" + page.getAlias() + "]成功", page.getName(), page.getAlias())));
					pageEvitKeySet.add(templateName);
				} else {
					// 可能需要更新页面
					Page current = optional.get();
					// 如果页面内容发生了改变或者页面是否启用发生了改变，此时需要更新页面
					if (!current.getTpl().equals(page.getTpl())
							|| Boolean.compare(current.isEnable(), page.isEnable()) != 0) {
						current.setTpl(page.getTpl());
						if (current.isEnable()) {
							helper.unregisterPage(current);
						}
						current.setEnable(page.isEnable());
						pageDao.update(current);
						if (page.isEnable()) {
							try {
								helper.registerPage(current);
							} catch (LogicException ex) {
								records.add(new ImportRecord(false, ex.getLogicMessage()));
								ts.setRollbackOnly();
								return records;
							}
						}
						records.add(new ImportRecord(ImportType.EDIT,
								new Message("import.update.page.success",
										"更新页面" + page.getName() + "[" + page.getAlias() + "]成功", page.getName(),
										page.getAlias())));

						pageEvitKeySet.add(templateName);
					} else {
						records.add(new ImportRecord(ImportType.NOCHANGE,
								new Message("import.page.nochange",
										"页面" + page.getName() + "[" + page.getAlias() + "]内容没有发生变化，无需更新",
										page.getName(), page.getAlias())));
					}
				}
			}

			for (Fragment fragment : fragments) {
				String fragmentName = fragment.getName();
				fragment.setSpace(space);
				// 查询当前的fragment
				Optional<Fragment> optionalFragment = queryFragmentWithTemplateName(fragment.getTemplateName());
				// 如果当前没有fragment，插入一个space级别的fragment
				if (!optionalFragment.isPresent()) {
					insertFragmentWhenImport(fragment, records);
					fragmentEvitKeySet.add(fragmentName);
				} else {
					Fragment currentFragment = optionalFragment.get();
					// 模版内容没有发生改变，无需变动
					if (currentFragment.getTpl().equals(fragment.getTpl())) {
						records.add(new ImportRecord(ImportType.NOCHANGE, new Message("import.fragment.nochange",
								"模板片段" + fragmentName + "内容没有发生变化，无需更新", fragmentName)));
					} else {
						// 如果是内置模板片段，插入新模板片段
						if (!currentFragment.hasId()) {
							insertFragmentWhenImport(fragment, records);
						} else {
							// 如果是global的，则插入space级别的
							if (currentFragment.isGlobal()) {
								insertFragmentWhenImport(fragment, records);
							} else {
								currentFragment.setTpl(fragment.getTpl());
								fragmentDao.update(currentFragment);
								records.add(
										new ImportRecord(ImportType.EDIT, new Message("import.update.fragment.success",
												"模板片段" + fragmentName + "更新成功", fragmentName)));
							}
						}
						fragmentEvitKeySet.add(fragmentName);
					}
				}
			}
			// 清空template 缓存
			evitPageCache(pageEvitKeySet.toArray(String[]::new));
			evitFragmentCache(fragmentEvitKeySet.toArray(String[]::new));
			return records;
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			ts.setRollbackOnly();
			records.add(new ImportRecord(false, Constants.SYSTEM_ERROR));
			return records;
		} finally {
			platformTransactionManager.commit(ts);
		}
	}

	/**
	 * 预览 导入的模板
	 * 
	 * @param spaceId
	 *            空间
	 * @param exportPages
	 * @throws LogicException
	 */
	@Override
	public synchronized PreviewImport previewImport(ExportPages exportPages) throws LogicException {
		List<ExportPage> exportPageList = exportPages.getPages();
		PreviewImport previewImport = new PreviewImport();
		if (CollectionUtils.isEmpty(exportPageList)) {
			return previewImport;
		}
		Space space = exportPages.getSpaceId() == null ? null : getRequiredSpace(exportPages.getSpaceId());
		Predicate<Page> filter = space == null ? page -> true : page -> !page.isSpaceGlobal();
		List<Page> pages = exportPageList.stream().map(ExportPage::getPage).filter(filter).collect(Collectors.toList());
		if (!pages.isEmpty()) {
			PreviewTemplateMapping previewTemplateMapping = templateMapping.getPreviewTemplateMapping();

			for (int i = 0; i < pages.size(); i++) {
				Page page = pages.get(i);
				page.setSpace(space);
				try {
					previewTemplateMapping.register(page);
					Page copy = new Page(page);
					copy.setTpl(null);
					previewImport.addPages(copy);
				} catch (PatternAlreadyExistsException e) {
					if (i > 0) {
						previewTemplateMapping.clear();
					}
					throw convert(e);
				}
			}
		}

		boolean inPreview = previewIp != null;

		List<Fragment> fragments = exportPageList.stream().flatMap(ep -> ep.getFragments().stream()).distinct()
				.collect(Collectors.toList());

		if (!fragments.isEmpty()) {
			for (int i = 0; i < fragments.size(); i++) {
				Fragment fragment = fragments.get(i);
				fragment.setSpace(space);
				try {
					registerPreview(fragment);
					Fragment copy = new Fragment(fragment);
					copy.setTpl(null);
					previewImport.addFragments(copy);
				} catch (LogicException e) {
					if (i > 0) {
						previewFragments.clear();

						if (!inPreview) {
							previewIp = null;
						}
					}
					throw e;
				}

			}
		}

		this.previewIp = Environment.getIP();

		return previewImport;
	}

	@Override
	public synchronized void registerPreview(Page page) throws LogicException {

		Space space = page.getSpace();
		if (space != null) {
			space = getRequiredSpace(space.getId());
			page.setSpace(space);
		}
		try {
			templateMapping.getPreviewTemplateMapping().register(page);
			previewIp = Environment.getIP();
		} catch (PatternAlreadyExistsException e) {
			throw convert(e);
		}
	}

	@Override
	public synchronized void registerPreview(Fragment fragment) throws LogicException {
		Space space = fragment.getSpace();
		if (space != null) {
			space = getRequiredSpace(space.getId());
			fragment.setSpace(space);
		}
		unregisterFragment(fragment);
		previewFragments.add(fragment);
		previewIp = Environment.getIP();
	}

	private void unregisterFragment(Fragment fragment) {
		String templateName = fragment.getTemplateName();
		for (Iterator<Fragment> it = previewFragments.iterator(); it.hasNext();) {
			Fragment pFragment = it.next();
			if (pFragment.getTemplateName().equals(templateName)) {
				// Template%Fragment%top%1
				if (pFragment.getSpace() != null || (pFragment.isGlobal() == fragment.isGlobal())) {
					it.remove();
					break;
				}
			}
		}
	}

	@Override
	public synchronized void clearPreview() {
		templateMapping.getPreviewTemplateMapping().clear();
		previewFragments.clear();
		previewIp = null;
	}

	/**
	 * 导入时候插入fragment
	 * 
	 * @param toImport
	 * @param records
	 */
	private void insertFragmentWhenImport(Fragment toImport, List<ImportRecord> records) {
		Fragment fragment = new Fragment();
		fragment.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
		fragment.setDescription("");
		fragment.setGlobal(false);
		fragment.setName(toImport.getName());
		fragment.setTpl(toImport.getTpl());
		fragment.setSpace(toImport.getSpace());
		fragmentDao.insert(fragment);
		records.add(new ImportRecord(ImportType.NEW,
				new Message("import.insert.tpl.success", "模板" + toImport.getName() + "插入成功", toImport.getName())));
	}

	@Override
	public PageStatistics queryPageStatistics(Space space) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, (status) -> {
			PageStatistics pageStatistics = new PageStatistics();
			TemplatePageQueryParam param = new TemplatePageQueryParam();
			param.setSpace(space);
			pageStatistics.setPageCount(pageDao.selectCount(param));

			return pageStatistics;
		});
	}

	@Override
	public void deleteHistoryTemplate(Integer id) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			HistoryTemplate db = historyTemplateDao.selectById(id);
			if (db == null) {
				throw new ResourceNotFoundException("historyTemplate.notExists", "历史模板不存在");
			}
			historyTemplateDao.deleteById(id);
		});
	}

	@Override
	public HistoryTemplate updateHistoryTemplate(Integer id, String remark) throws LogicException {
		return Transactions.executeInTransaction(platformTransactionManager, status -> {
			HistoryTemplate db = historyTemplateDao.selectById(id);
			if (db == null) {
				throw new ResourceNotFoundException("historyTemplate.notExists", "历史模板不存在");
			}
			db.setRemark(remark);
			historyTemplateDao.update(db);

			db.setTpl(null);

			return db;
		});
	}

	@Override
	public void savePageHistory(Integer id, String remark) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			Page db = getRequiredPage(id, LogicException::new);
			HistoryTemplate historyTemplate = new HistoryTemplate();
			historyTemplate.setTemplateId(db.getId());
			historyTemplate.setType(HistoryTemplateType.PAGE);
			historyTemplate.setRemark(remark);
			historyTemplate.setTime(Timestamp.valueOf(Times.now()));
			historyTemplate.setTpl(db.getTpl());

			historyTemplateDao.insert(historyTemplate);
		});
	}

	@Override
	public void saveFragmentHistory(Integer id, String remark) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			Fragment db = getRequiredFragment(id, LogicException::new);
			HistoryTemplate historyTemplate = new HistoryTemplate();
			historyTemplate.setTemplateId(db.getId());
			historyTemplate.setType(HistoryTemplateType.FRAGMENT);
			historyTemplate.setRemark(remark);
			historyTemplate.setTime(Timestamp.valueOf(Times.now()));
			historyTemplate.setTpl(db.getTpl());

			historyTemplateDao.insert(historyTemplate);
		});
	}

	@Override
	public List<HistoryTemplate> queryPageHistory(Integer id) throws LogicException {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			Page page = pageDao.selectById(id);
			if (page == null) {
				throw new ResourceNotFoundException("page.user.notExists", "自定义页面不存在");
			}
			return page == null ? new ArrayList<>()
					: historyTemplateDao.selectByTemplate(page.getId(), HistoryTemplateType.PAGE);
		});
	}

	/**
	 * 查询某个模板片段的历史模板
	 * 
	 * @param id
	 * @return
	 */
	public List<HistoryTemplate> queryFragmentHistory(Integer id) throws LogicException {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			Fragment fragment = fragmentDao.selectById(id);
			if (fragment == null) {
				throw new ResourceNotFoundException("fragment.user.notExists", "模板片段不存在");
			}
			return historyTemplateDao.selectByTemplate(fragment.getId(), HistoryTemplateType.FRAGMENT);
		});
	}

	/**
	 * 查询历史模板详情
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Optional<HistoryTemplate> getHistoryTemplate(Integer id) {
		return Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			return Optional.ofNullable(historyTemplateDao.selectById(id));
		});
	}

	@Override
	public boolean isPreviewIp(String ip) {
		return previewIp != null && previewIp.equals(ip);
	}

	/**
	 * 容器重新启动时载入mapping
	 * 
	 * @param evt
	 * @throws Exception
	 */
	@EventListener
	public void handleContextRefreshEvent(ContextRefreshedEvent evt) throws Exception {
		if (evt.getApplicationContext().getParent() == null) {
			return;
		}

		WebApplicationContext applicationContext = (WebApplicationContext) evt.getApplicationContext();
		AbstractApplicationContext appContext = (AbstractApplicationContext) applicationContext.getParent();
		appContext.addApplicationListener(new SpaceDeleteEventListener());

		Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			PageRequestMappingRegisterHelper helper = new PageRequestMappingRegisterHelper();
			List<Page> allPage = pageDao.selectAll();
			for (Page page : allPage) {
				if (page.isEnable()) {
					try {
						helper.registerPage(page);
					} catch (LogicException e) {
						throw new SystemException(page.getRelativePath() + "已经存在了");
					}
				}
			}
		});
	}

	/**
	 * 清空缓存时删除预览模板
	 * 
	 * @param evt
	 */
	@EventListener
	public void handleTemplateEvitEvent(TemplateEvitEvent evt) {
		if (evt.clear()) {
			clearPreview();
		} else {
			synchronized (this) {
				String[] templateNames = evt.getTemplateNames();
				templateMapping.getPreviewTemplateMapping().unregister(templateNames);
				previewFragments.removeIf(fragment -> {
					for (String templateName : templateNames) {
						if (templateName.equals(fragment.getTemplateName())) {
							return true;
						}
					}
					return false;
				});

			}
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		initSystemTemplates();

		// System Template Processor
		this.templateProcessors.add(new SystemTemplateProcessor());
		// Page Template Processor
		this.templateProcessors.add(new PageTemplateProcessor());
		// Fragment Template Processor
		this.templateProcessors.add(new FragmentTemplateProcessor());

		if (commentServer == null) {
			commentServer = EmptyCommentServer.INSTANCE;
		}

	}

	/**
	 * 初始化系统默认模板，这些模板都能被删除
	 * 
	 * @throws Exception
	 */
	private void initSystemTemplates() throws Exception {
		defaultTemplates = new HashMap<>();
		// 博客主页
		defaultTemplates.put("", new SystemTemplate("", new ClassPathResource("resources/page/PAGE_INDEX.html")));
		// 博客登录页
		defaultTemplates.put("login", new SystemTemplate("login", new ClassPathResource("resources/page/LOGIN.html")));
		// 各个空间的主页
		defaultTemplates.put("space/{alias}",
				new SystemTemplate("space/{alias}", new ClassPathResource("resources/page/PAGE_INDEX.html")));
		// 各个空间文章详情页面
		defaultTemplates.put("space/{alias}/article/{idOrAlias}", new SystemTemplate(
				"space/{alias}/article/{idOrAlias}", new ClassPathResource("resources/page/PAGE_ARTICLE_DETAIL.html")));

		defaultTemplates.put("error/{errorCode}",
				new SystemTemplate("error/{errorCode}", new ClassPathResource("resources/page/PAGE_ERROR.html")));
		// 各个空间错误显示页面
		defaultTemplates.put("space/{alias}/error/{errorCode}", new SystemTemplate("space/{alias}/error/{errorCode}",
				new ClassPathResource("resources/page/PAGE_ERROR.html")));

		defaultTemplates.put("news",
				new SystemTemplate("news", new ClassPathResource("resources/page/PAGE_NEWS.html")));
		defaultTemplates.put("news/{id}",
				new SystemTemplate("news/{id}", new ClassPathResource("resources/page/PAGE_NEWS_DETAIL.html")));

		for (Map.Entry<String, SystemTemplate> it : defaultTemplates.entrySet()) {
			templateMapping.register(it.getKey(), it.getValue().getTemplateName());
		}

	}

	private void evitPageCache(String... templateNames) {
		if (templateNames != null && templateNames.length > 0) {
			Transactions.afterCommit(
					() -> this.applicationEventPublisher.publishEvent(new TemplateEvitEvent(this, templateNames)));
		}
	}

	private void evitPageCache(Page... pages) {
		evitPageCache(Arrays.stream(pages).map(Page::getTemplateName).toArray(String[]::new));
	}

	private void evitFragmentCache(String... names) {
		Transactions.afterCommit(() -> {
			if (Validators.isEmpty(names)) {
				return;
			}

			// fragment比较特殊，它是按照名称来区分的，尝试fragment的缓存时
			// 需要删除各个空间中存在该名称的fragment缓存
			List<Space> spaces = spaceDao.selectByParam(new SpaceQueryParam());
			Set<String> templateNames = new HashSet<>();
			for (String name : names) {
				templateNames.add(Fragment.getTemplateName(name, null));
				for (Space space : spaces) {
					templateNames.add(Fragment.getTemplateName(name, space));
				}
			}
			this.applicationEventPublisher
					.publishEvent(new TemplateEvitEvent(this, templateNames.toArray(String[]::new)));
		});
	}

	private Optional<Fragment> queryFragmentWithTemplateName(String templateName) {
		String[] array = templateName.split(Template.SPLITER);
		String name;
		Space space = null;
		if (array.length == 3) {
			name = array[2];
		} else if (array.length == 4) {
			name = array[2];
			space = new Space(Integer.parseInt(array[3]));
		} else {
			throw new SystemException(templateName + "无法转化为Fragment");
		}

		Fragment del = null;

		Fragment fragment = fragmentDao.selectBySpaceAndName(space, name);
		if (fragment == null || fragment.isDel() || !fragment.isEnable()) { // 查找全局
			if (fragment != null && fragment.isDel()) {
				del = new Fragment(fragment);
			}
			fragment = fragmentDao.selectGlobalByName(name);
		}

		if (fragment == null || fragment.isDel() || !fragment.isEnable()) {
			if (fragment != null && fragment.isDel() && del == null) {
				del = new Fragment(fragment);
			}
			// 查找内置模板片段
			// 为了防止默认模板片段被修改，这里首先进行clone
			fragment = fragments.stream().filter(fb -> fb.getName().equals(name)).findAny().map(Fragment::new)
					.orElse(null);
		}

		if (fragment == null && del != null) {
			return Optional.of(del);
		}
		return Optional.ofNullable(fragment);
	}

	// Template%Page%{alias}%{spaceGlobal}[%{space.id}]
	private Optional<Page> queryPageWithTemplateName(String templateName) {
		Page page;
		String[] array = templateName.split(Template.SPLITER);
		if (array.length == 4) {
			page = pageDao.selectBySpaceAndAlias(null, array[2], Boolean.parseBoolean(array[3]));
		} else if (array.length == 5) {
			page = pageDao.selectBySpaceAndAlias(new Space(Integer.parseInt(array[4])), array[2], false);
		} else {
			throw new SystemException(templateName + "无法转化为用户自定义页面");
		}
		return Optional.ofNullable(page);
	}

	private ExportPage export(Page page) {
		ExportPage exportPage = new ExportPage();
		exportPage.setPage(page.toExportPage());
		Map<String, Fragment> fragmentMap = new HashMap<>();
		fillMap(fragmentMap, page.getSpace(), page.getTpl());
		for (Fragment fragment : fragmentMap.values()) {
			if (fragment != null) {
				exportPage.add(fragment.toExportFragment());
			}
		}
		fragmentMap.clear();
		return exportPage;
	}

	private void fillMap(Map<String, Fragment> fragmentMap, Space space, String tpl) {
		Map<String, Fragment> fragmentMap2 = new HashMap<>();
		Document document = Jsoup.parse(tpl);
		Elements elements = document.getElementsByTag("fragment");
		for (Element element : elements) {
			String name = element.attr("name");
			if (fragmentMap.containsKey(name)) {
				continue;
			}
			Optional<Fragment> optional = queryFragmentWithTemplateName(Fragment.getTemplateName(name, space));
			fragmentMap.put(name, optional.filter(fra -> !fra.isDel()).orElse(null));
			optional.ifPresent(fragment -> fragmentMap2.put(name, fragment));
		}
		for (Map.Entry<String, Fragment> fragmentIterator : fragmentMap2.entrySet()) {
			Fragment value = fragmentIterator.getValue();
			fillMap(fragmentMap, space, value.getTpl());
		}
		fragmentMap2.clear();
	}

	/**
	 * 用来在一个<b>事务</b>中使mapping和页面保持一致
	 * 
	 * @author mhlx
	 *
	 */
	private final class PageRequestMappingRegisterHelper {

		private final List<Runnable> rollBackActions = new ArrayList<>();

		public PageRequestMappingRegisterHelper() {
			super();

			if (!TransactionSynchronizationManager.isSynchronizationActive()) {
				throw new SystemException(this.getClass().getName() + " 必须处于一个事务中");
			}

			templateMapping.getLock().lock();

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

				@Override
				public void afterCompletion(int status) {
					try {
						if (status == STATUS_ROLLED_BACK) {
							rollback();
						}
					} finally {
						templateMapping.getLock().unlock();
					}
				}

				/**
				 * 这里必须最高的优先级，第一时间解锁
				 */
				@Override
				public int getOrder() {
					return Ordered.HIGHEST_PRECEDENCE;
				}

			});
		}

		void registerPage(Page page) throws LogicException {
			String path = page.getRelativePath();
			try {
				templateMapping.register(path, page.getTemplateName());
			} catch (PatternAlreadyExistsException e) {
				throw convert(e);
			}
			rollBackActions.add(() -> templateMapping.unregister(path));
		}

		void unregisterPage(Page page) {
			String path = page.getRelativePath();
			if (templateMapping.unregister(path)) {
				rollBackActions.add(() -> templateMapping.forceRegisterTemplateMapping(path, page.getTemplateName()));
			}
		}

		private void rollback() {
			if (!rollBackActions.isEmpty()) {
				for (Runnable act : rollBackActions) {
					try {
						act.run();
					} catch (Throwable e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	private final class SpaceDeleteEventListener implements ApplicationListener<SpaceDelEvent> {

		@Override
		public void onApplicationEvent(SpaceDelEvent event) {
			synchronized (TemplateServiceImpl.this) {
				// 删除所有的fragments
				List<Fragment> fragments = fragmentDao.selectBySpace(event.getSpace());
				for (Fragment fragment : fragments) {
					historyTemplateDao.deleteByTemplate(fragment.getId(), HistoryTemplateType.FRAGMENT);
					fragmentDao.deleteById(fragment.getId());
				}
				// 事务结束之后清空所有页面缓存
				Transactions.afterCommit(() -> applicationEventPublisher.publishEvent(new TemplateEvitEvent(this)));
				// 查询所有的页面
				List<Page> pages = pageDao.selectBySpace(event.getSpace());
				if (!pages.isEmpty()) {
					PageRequestMappingRegisterHelper helper = new PageRequestMappingRegisterHelper();
					for (Page page : pages) {
						historyTemplateDao.deleteByTemplate(page.getId(), HistoryTemplateType.PAGE);
						pageDao.deleteById(page.getId());
						// 解除mapping注册
						helper.unregisterPage(page);
					}
					// 发送事件
					applicationEventPublisher.publishEvent(new PageDelEvent(this, pages));
				}
			}
		}

	}

	public void setProcessors(List<DataTagProcessor<?>> processors) {
		// 查找重复dataName
		Set<String> dataNames = new HashSet<>();
		processors.stream()
				.filter(processor -> !dataNames.add(processor.getDataName()) || !dataNames.add(processor.getName()))
				.findAny().ifPresent(processor -> {
					throw new SystemException(
							"DataTagProcessor数据名称:" + processor.getName() + "或者" + processor.getName() + "存在重复");
				});

		for (DataTagProcessor<?> processor : processors) {
			if (!Validators.isLetterOrNumOrChinese(processor.getName())) {
				throw new SystemException("数据名只能为中英文或者数字");
			}
			if (!DataTagProcessor.validDataName(processor.getDataName())) {
				throw new SystemException("数据dataName只能为英文字母或者数字，并且不能以数字开头");
			}
		}

		Map<String, Boolean> callableMap = readCallableMap();
		processors.forEach(pro -> {
			Boolean callable = callableMap.get(pro.getName());
			if (callable != null) {
				pro.setCallable(callable);
			}
		});
		this.processors = processors;
	}

	/**
	 * 设置系统内置的fragment
	 * <p>
	 * <b>无论是否设置space，这些fragment都是全局的</b>
	 * </p>
	 * 
	 * @param fragments
	 */
	public void setFragments(List<Fragment> fragments) {
		for (Fragment fragment : fragments) {
			// 清除ID，用来判断是否是内置模板片段
			fragment.setId(null);
			fragment.setEnable(true);
			this.fragments.add(fragment);
		}
	}

	private LogicException convert(PatternAlreadyExistsException ex) {
		String pattern = ex.getPattern();
		if (ex.isKeyPath()) {
			return new LogicException("templateMapping.register.path.keyPath", "路径" + pattern + "是系统保留路径", pattern);
		}
		if (ex.getMatchPattern() == null || pattern.equals(ex.getMatchPattern())) {
			return new LogicException("templateMapping.register.path.exists", "路径" + pattern + "已经存在", pattern);
		} else {
			return new LogicException("templateMapping.register.path.match",
					"路径" + pattern + "已经存在匹配路径:" + ex.getMatchPattern(), pattern, ex.getMatchPattern());
		}
	}

	private interface TemplateProcessor {
		/**
		 * 是否能够处理该模板
		 * 
		 * @param templateSign
		 * @return
		 */
		boolean canProcess(String templateSign);

		/**
		 * 根据模板名查询模板
		 * 
		 * @param templateName
		 *            模板名
		 * @return 模板，如果不存在，返回null
		 */
		Template getTemplate(String templateName);

	}

	private final class SystemTemplateProcessor implements TemplateProcessor {
		@Override
		public Template getTemplate(String templateName) {
			String[] array = templateName.split(Template.SPLITER);
			String path;
			if (array.length == 3) {
				path = array[2];
			} else if (array.length == 2) {
				path = "";
			} else {
				throw new SystemException("无法从" + templateName + "中获取路径");
			}
			SystemTemplate template = defaultTemplates.get(path);
			if (template != null) {
				template = template.cloneTemplate();
			}
			return template;
		}

		@Override
		public boolean canProcess(String templateSign) {
			return SystemTemplate.isSystemTemplate(templateSign);
		}
	}

	private final class PageTemplateProcessor implements TemplateProcessor {

		@Override
		public Template getTemplate(String templateName) {
			Template template = null;
			Optional<String> op = Page.getOriginalTemplateFromPreviewTemplateName(templateName);
			String originalTemplateName = op.orElse(templateName);
			if (op.isPresent()) {
				template = templateMapping.getPreviewTemplateMapping().getPreviewTemplate(originalTemplateName)
						.orElse(null);
			}
			if (template == null) {
				template = queryPageWithTemplateName(originalTemplateName).filter(Page::isEnable).orElse(null);
			}
			return template;
		}

		@Override
		public boolean canProcess(String templateSign) {
			return Page.isPageTemplate(templateSign)
					|| Page.getOriginalTemplateFromPreviewTemplateName(templateSign).isPresent();
		}
	}

	private final class FragmentTemplateProcessor implements TemplateProcessor {

		@Override
		public Template getTemplate(String templateName) {
			Optional<String> op = Fragment.getOriginalTemplateFromPreviewTemplateName(templateName);
			String originalTemplateName = op.orElse(templateName);
			if (op.isPresent()) {
				synchronized (TemplateServiceImpl.this) {
					if (!previewFragments.isEmpty()) {
						Fragment best = null;
						for (Fragment previewFragment : previewFragments) {
							if (previewFragment.getTemplateName().equals(originalTemplateName)) {
								if (!previewFragment.isGlobal() || previewFragment.getSpace() != null) {
									best = previewFragment;
									break;
								}
								best = previewFragment;
							}
						}
						if (best != null) {
							return best;
						}
					}
				}
			}
			return queryFragmentWithTemplateName(originalTemplateName).orElse(null);
		}

		@Override
		public boolean canProcess(String templateSign) {
			return Fragment.isFragmentTemplate(templateSign)
					|| Fragment.getOriginalTemplateFromPreviewTemplateName(templateSign).isPresent();
		}
	}

	private Optional<TemplateProcessor> getTemplateProcessor(String templateSign) {
		for (TemplateProcessor processor : templateProcessors) {
			if (processor.canProcess(templateSign)) {
				return Optional.of(processor);
			}
		}
		return Optional.empty();
	}

	private Page getRequiredPage(Integer id, Function<Message, LogicException> exFunction) throws LogicException {
		Page db = pageDao.selectById(id);
		if (db == null) {
			throw exFunction.apply(new Message("page.user.notExists", "自定义页面不存在"));
		}
		return db;
	}

	private Fragment getRequiredFragment(Integer id, Function<Message, LogicException> exFunction)
			throws LogicException {
		Fragment fragment = fragmentDao.selectById(id);
		if (fragment == null || fragment.isDel()) {
			throw exFunction.apply(new Message("fragment.user.notExists", "模板片段不存在"));
		}
		return fragment;
	}

	@Override
	public DataTagProcessorRegistry register(DataTagProcessor<?> processor) {
		processors.stream().filter(
				pro -> pro.getDataName().equals(processor.getDataName()) || pro.getName().equals(processor.getName()))
				.findAny().ifPresent(pro -> {
					throw new SystemException(
							"DataTagProcessor数据名称:" + processor.getName() + "或者" + processor.getDataName() + "存在重复");
				});
		Boolean callable = readCallableMap().get(processor.getDataName());
		if (callable != null) {
			processor.setCallable(callable);
		}
		processors.add(processor);
		return this;
	}

	@Override
	public TemplateRegistry registerSystemTemplate(String path, String template) {
		synchronized (this) {
			String clean = FileUtils.cleanPath(path);
			if (templateMapping.isKeyPath(clean)) {
				throw new SystemException("路径" + clean + "为系统保留路径");
			}
			SystemTemplate systemTemplate = new SystemTemplate(clean, template);
			if (defaultTemplates.containsKey(clean)) {
				// replace
				defaultTemplates.put(clean, systemTemplate);
			} else {
				// add
				try {
					templateMapping.register(clean, systemTemplate.getTemplateName());
					defaultTemplates.put(clean, systemTemplate);
				} catch (PatternAlreadyExistsException e) {
					// 忽略这个异常，可能被用户覆盖
				}
			}
			return this;
		}
	}

	@Override
	public TemplateRegistry registerGlobalFragment(String name, String template, boolean callable) {
		this.fragments.removeIf(fragment -> fragment.getName().equals(name));
		Fragment fragment = new Fragment(name);
		fragment.setTpl(template);
		fragment.setCallable(callable);
		this.fragments.add(fragment);
		return this;
	}

	@Override
	public List<SystemTemplate> getSystemTemplates() {
		return List.copyOf(new ArrayList<>(defaultTemplates.values()));
	}

	@Override
	public List<Fragment> getDefaultFragment() {
		return Collections.unmodifiableList(fragments);
	}

	@Override
	public void updateDataCallable(String name, boolean callable) {
		synchronized (this) {
			Optional<DataTagProcessor<?>> op = processors.stream()
					.filter(pro -> pro.getName().equals(name) || pro.getDataName().equals(name)).findAny();
			if (op.isPresent()) {
				DataTagProcessor<?> processor = op.get();

				if (processor.isCallable() == callable) {
					return;
				}
				processor.setCallable(callable);

				Map<String, Boolean> map = processors.stream()
						.collect(Collectors.toMap(DataTagProcessor::getName, DataTagProcessor::isCallable));

				try (Writer writer = Files.newBufferedWriter(DATA_CONFIG, Constants.CHARSET)) {
					Jsons.write(map, writer);
				} catch (IOException e) {

					processor.setCallable(!callable);

					throw new SystemException(e.getMessage(), e);
				}

			}
		}
	}

	@Override
	public synchronized void disablePageByPath(String path) throws LogicException {
		Transactions.executeInTransaction(platformTransactionManager, status -> {
			Optional<String> optional = templateMapping.getTemplateNameEqualsPattern(FileUtils.cleanPath(path));
			if (optional.isPresent()) {
				String templateName = optional.get();
				if (Page.isPageTemplate(templateName)) {
					Optional<Page> opPage = queryPageWithTemplateName(templateName);
					if (opPage.isPresent()) {
						Page page = opPage.get();
						Page copy = new Page(page);
						page.setEnable(false);
						pageDao.update(page);
						new PageRequestMappingRegisterHelper().unregisterPage(page);
						this.applicationEventPublisher.publishEvent(new PageUpdateEvent(this, copy, page));
					}
				}
			}
		});
	}

	private Map<String, Boolean> readCallableMap() {
		try {
			String content = FileUtils.toString(DATA_CONFIG);
			if (!Validators.isEmptyOrNull(content, false)) {
				Type type = new TypeToken<Map<String, Boolean>>() {
				}.getType();
				return Jsons.getGson().fromJson(content, type);
			}
			return Collections.emptyMap();
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	private Space getRequiredSpace(Integer id) throws LogicException {
		Space space = spaceDao.selectById(id);
		if (space == null) {
			throw new LogicException("space.notExists", "空间不存在");
		}
		return space;
	}
}
