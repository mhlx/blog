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
package me.qyh.blog.plugin.comment.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.text.CommonMarkdown2Html;
import me.qyh.blog.core.text.HtmlClean;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.CommentModuleStatistics;
import me.qyh.blog.core.vo.CommentStatistics;
import me.qyh.blog.core.vo.Limit;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.dao.CommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.Comment.CommentStatus;
import me.qyh.blog.plugin.comment.entity.CommentMode;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.event.CommentEvent;
import me.qyh.blog.plugin.comment.vo.CommentPageResult;
import me.qyh.blog.plugin.comment.vo.CommentQueryParam;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;
import me.qyh.blog.plugin.comment.vo.PeriodCommentQueryParam;

public class CommentService implements InitializingBean, CommentServer, ApplicationEventPublisherAware {

	private List<CommentChecker> checkers = new ArrayList<>();
	@Autowired(required = false)
	private HtmlClean htmlClean;
	@Autowired
	protected CommentDao commentDao;
	@Autowired(required = false)
	private Markdown2Html markdown2Html;
	@Autowired
	private UserService userService;
	@Autowired
	private UrlHelper urlHelper;

	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 评论配置项
	 */
	private static final String COMMENT_EDITOR = "commentConfig.editor";
	private static final String COMMENT_LIMIT_SEC = "commentConfig.commentLimitSec";
	private static final String COMMENT_LIMIT_COUNT = "commentConfig.commentLimitCount";
	private static final String COMMENT_CHECK = "commentConfig.commentCheck";
	private static final String COMMENT_PAGESIZE = "commentConfig.pageSize";
	private static final String COMMENT_NICKNAME = "commentConfig.nickname";

	private final Comparator<Comment> ascCommentComparator = Comparator.comparing(Comment::getCommentDate)
			.thenComparing(Comment::getId);
	private final Comparator<Comment> descCommentComparator = (t1, t2) -> -ascCommentComparator.compare(t1, t2);

	/**
	 * 为了保证一个树结构，这里采用 path来纪录层次结构
	 * {@link http://stackoverflow.com/questions/4057947/multi-tiered-comment-replies-display-and-storage}.
	 * 同时为了走索引，只能限制它为255个字符，由于id为数字的原因，实际上一般情况下很难达到255的长度(即便id很大)，所以这里完全够用
	 */
	private static final int PATH_MAX_LENGTH = 255;

	/**
	 * 评论配置文件位置
	 */

	private static final Path RES_PATH = Constants.CONFIG_DIR.resolve("commentConfig.properties");
	private final Resource configResource = new FileSystemResource(RES_PATH);
	private final Properties pros = new Properties();

	private CommentConfig config;

	private final Map<String, CommentModuleHandler> handlerMap = new HashMap<>();

	@Autowired(required = false)
	private BlacklistHandler blacklistHandler;

	static {
		FileUtils.createFile(RES_PATH);
	}

	/**
	 * 审核评论
	 * 
	 * @param id
	 *            评论id
	 * @return
	 * @throws LogicException
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void changeStatus(Integer id, CommentStatus status) throws LogicException {
		Comment comment = commentDao.selectById(id);// 查询父评论
		if (comment == null) {
			throw new LogicException("comment.notExists", "评论不存在");
		}
		commentDao.updateStatus(id, status);
	}

	/**
	 * 删除评论
	 * 
	 * @param id
	 *            评论id
	 * @throws LogicException
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteComment(Integer id) throws LogicException {
		Comment comment = commentDao.selectById(id);
		if (comment == null) {
			throw new LogicException("comment.notExists", "评论不存在");
		}
		commentDao.deleteByPath(comment.getParentPath() + comment.getId());
		commentDao.deleteById(id);
	}

	/**
	 * 更新评论配置
	 * 
	 * @param config
	 *            配置
	 */
	public synchronized CommentConfig updateCommentConfig(CommentConfig config) {
		pros.setProperty(COMMENT_EDITOR, config.getEditor().name());
		pros.setProperty(COMMENT_CHECK, config.getCheck().toString());
		pros.setProperty(COMMENT_LIMIT_COUNT, config.getLimitCount().toString());
		pros.setProperty(COMMENT_LIMIT_SEC, config.getLimitSec().toString());
		pros.setProperty(COMMENT_PAGESIZE, String.valueOf(config.getPageSize()));
		pros.setProperty(COMMENT_NICKNAME, config.getNickname());
		try (OutputStream os = new FileOutputStream(configResource.getFile())) {
			pros.store(os, "");
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		loadConfig();
		return config;
	}

	/**
	 * 获取评论配置
	 * 
	 * @return
	 */
	public CommentConfig getCommentConfig() {
		return new CommentConfig(config);
	}

	/**
	 * 新增一条评论
	 * 
	 * @param comment
	 * @return
	 * @throws LogicException
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Comment insertComment(Comment comment) throws LogicException {
		CommentModule module = comment.getCommentModule();

		CommentModuleHandler handler = handlerMap.get(module.getModule());
		if (handler == null) {
			throw new LogicException("comment.module.invalid", "评论模块不存在");
		}
		handler.doValidateBeforeInsert(module.getId());

		long now = System.currentTimeMillis();
		String ip = comment.getIp();
		if (!Environment.hasAuthencated()) {

			if (blacklistHandler.match(ip)) {
				throw new LogicException("comment.ip.forbidden", "ip被禁止评论");
			}

			// 检查频率
			Limit limit = config.getLimit();
			long start = now - limit.getUnit().toMillis(limit.getTime());
			int count = commentDao.selectCountByIpAndDatePeriod(comment.getCommentModule(), new Timestamp(start),
					new Timestamp(now), ip) + 1;
			if (count > limit.getCount()) {
				throw new LogicException("comment.overlimit", "评论太过频繁，请稍作休息");
			}

			for (CommentChecker checker : checkers) {
				checker.checkComment(new Comment(comment), new CommentConfig(config));
			}
		}

		String parentPath = "/";
		// 判断是否存在父评论
		Comment parent = comment.getParent();
		if (parent != null) {
			parent = commentDao.selectById(parent.getId());// 查询父评论
			if (parent == null) {
				throw new LogicException("comment.parent.notExists", "父评论不存在");
			}

			// 如果父评论正在审核
			if (parent.isChecking()) {
				throw new LogicException("comment.parent.checking", "父评论正在审核");
			}

			if (!comment.matchParent(parent)) {
				throw new LogicException("comment.parent.unmatch", "评论匹配失败");
			}
			parentPath = parent.getParentPath() + parent.getId() + "/";
		}
		if (parentPath.length() > PATH_MAX_LENGTH) {
			throw new LogicException("comment.path.toolong", "该评论不能再被回复了");
		}

		Comment last = commentDao.selectLast(comment);
		if (last != null && last.getContent().equals(comment.getContent())) {
			throw new LogicException("comment.content.same", "已经回复过相同的评论了");
		}

		if (!Environment.hasAuthencated()) {
			String email = comment.getEmail();
			if (email != null) {
				// set gravatar md5
				comment.setGravatar(DigestUtils.md5DigestAsHex(email.getBytes(Constants.CHARSET)));
			}
			comment.setAdmin(false);

		} else {
			// 管理员回复无需设置评论用户信息
			comment.setEmail(null);
			comment.setNickname(null);
			comment.setAdmin(true);
			comment.setWebsite(null);
		}

		comment.setParentPath(parentPath);
		comment.setCommentDate(new Timestamp(now));

		boolean check = config.getCheck() && !Environment.hasAuthencated();
		comment.setStatus(check ? CommentStatus.CHECK : CommentStatus.NORMAL);
		// 获取当前设置的编辑器
		comment.setEditor(config.getEditor());
		comment.setParent(parent);

		commentDao.insert(comment);

		handleComment(comment);
		handleCommentsContent(List.of(comment));

		applicationEventPublisher.publishEvent(new CommentEvent(this, comment));

		return comment;
	}

	/**
	 * 分页查询评论
	 * 
	 * @param param
	 * @return
	 */
	@Transactional(readOnly = true)
	public CommentPageResult queryComment(CommentQueryParam param) {
		if (!param.complete()) {
			return new CommentPageResult(param, 0, new ArrayList<>(), new CommentConfig(config));
		}
		CommentModule module = param.getModule();
		CommentModuleHandler handler = handlerMap.get(module.getModule());
		if (handler == null || !handler.doValidateBeforeQuery(module.getId())) {
			return new CommentPageResult(param, 0, new ArrayList<>(), new CommentConfig(config));
		}

		CommentMode mode = param.getMode();
		int count;
		switch (mode) {
		case TREE:
			count = commentDao.selectCountWithTree(param);
			break;
		default:
			count = commentDao.selectCountWithList(param);
			break;
		}
		int pageSize = param.getPageSize();
		if (count == 0) {
			return new CommentPageResult(param, 0, new ArrayList<>(), new CommentConfig(config));
		}
		boolean asc = param.isAsc();
		if (param.getCurrentPage() <= 0) {
			if (asc) {
				param.setCurrentPage(count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
			} else {
				param.setCurrentPage(1);
			}
		}
		List<Comment> datas;
		switch (mode) {
		case TREE:
			datas = commentDao.selectPageWithTree(param);
			for (Comment comment : datas) {
				handleComment(comment);
			}
			handleCommentsContent(datas);
			datas = handleTree(datas, param.isAsc());
			break;
		default:
			datas = commentDao.selectPageWithList(param);
			for (Comment comment : datas) {
				handleComment(comment);
			}
			handleCommentsContent(datas);
			break;
		}

		CommentPageResult result = new CommentPageResult(param, count, datas, new CommentConfig(config));

		if (Environment.hasAuthencated()) {
			CommentQueryParam copy = new CommentQueryParam(param);
			copy.setStatus(CommentStatus.CHECK);
			switch (mode) {
			case TREE:
				result.setCheckCount(commentDao.selectCountWithTree(copy));
				break;
			default:
				result.setCheckCount(commentDao.selectCountWithList(copy));
				break;
			}
		}
		return result;
	}

	@Transactional(readOnly = true)
	public PageResult<Comment> queryAllCommentsByPeriod(PeriodCommentQueryParam param) {
		int count = commentDao.selectCountByPeriod(param);
		List<Comment> datas = commentDao.selectPageByPeriod(param);
		datas.forEach(this::handleComment);
		handleCommentsContent(datas);
		return new PageResult<>(param, count, datas);
	}

	/**
	 * 删除某个模块的评论
	 * 
	 * @param module
	 *            模块
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteComment(CommentModule module) {
		commentDao.deleteByModule(module);
	}

	/**
	 * 查询<b>当前空间</b>下 某个模块类型的最近的评论
	 * <p>
	 * <b>用于DataTag</b>
	 * </p>
	 * 
	 * @param module
	 * @param limit
	 * @param queryAdmin
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Comment> queryLastComments(String module, int limit, boolean queryAdmin) {
		if (Validators.isEmptyOrNull(module, true)) {
			return new ArrayList<>();
		}
		CommentModuleHandler handler = handlerMap.get(module);
		if (handler == null) {
			return new ArrayList<>();
		}
		List<Comment> comments = handler.queryLastComments(Environment.getSpace(), limit, Environment.hasAuthencated(),
				queryAdmin);
		for (Comment comment : comments) {
			handleComment(comment);
		}
		handleCommentsContent(comments);
		return comments;
	}

	/**
	 * 查询会话
	 * 
	 * @return
	 * @throws LogicException
	 */
	@Transactional(readOnly = true)
	public List<Comment> queryConversations(CommentModule module, Integer id) throws LogicException {
		if (module.getModule() == null || module.getId() == null) {
			return new ArrayList<>();
		}
		CommentModuleHandler handler = handlerMap.get(module.getModule());
		if (handler == null || !handler.doValidateBeforeQuery(module.getId())) {
			return new ArrayList<>();
		}
		Comment comment = commentDao.selectById(id);
		if (comment == null) {
			throw new LogicException("comment.notExists", "评论不存在");
		}
		if (!comment.getCommentModule().equals(module)) {
			return new ArrayList<>();
		}
		handleComment(comment);
		List<Comment> comments = new ArrayList<>();
		if (!comment.getParents().isEmpty()) {
			for (Integer pid : comment.getParents()) {
				Comment p = commentDao.selectById(pid);
				handleComment(p);
				comments.add(p);
			}
		}
		comments.add(comment);
		handleCommentsContent(comments);
		return comments;
	}

	@Override
	@Transactional(readOnly = true)
	public OptionalInt queryCommentNum(String module, Integer moduleId) {
		CommentModuleHandler handler = handlerMap.get(module);
		if (handler != null) {
			return handler.queryCommentNum(moduleId);
		}
		return OptionalInt.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Integer> queryCommentNums(String module, Collection<Integer> moduleIds) {
		CommentModuleHandler handler = handlerMap.get(module);
		if (handler != null) {
			return handler.queryCommentNums(moduleIds);
		}
		return new HashMap<>();
	}

	@Override
	@Transactional(readOnly = true)
	public OptionalInt queryCommentNum(String module, Space space, boolean queryPrivate) {
		CommentModuleHandler handler = handlerMap.get(module);
		if (handler != null) {
			return OptionalInt.of(handler.queryCommentNum(space, queryPrivate));
		}
		return OptionalInt.empty();
	}

	/**
	 * 查询评论统计情况
	 * 
	 * @param space
	 *            当前空间
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public CommentStatistics queryCommentStatistics(Space space) {
		CommentStatistics commentStatistics = new CommentStatistics();
		boolean queryPrivate = Environment.hasAuthencated();
		for (CommentModuleHandler handler : handlerMap.values()) {
			commentStatistics.addModule(new CommentModuleStatistics(handler.getModuleName(), handler.getName(),
					handler.queryCommentNum(space, queryPrivate)));
		}
		return commentStatistics;
	}

	/**
	 * 查询某个评论模块项目的地址
	 * 
	 * @param module
	 * @return
	 */
	@Transactional(readOnly = true)
	public Optional<String> queryCommentModuleUrl(CommentModule module) {
		CommentModuleHandler handler = handlerMap.get(module.getModule());

		if (handler != null) {
			return handler.getUrl(module.getId());
		}

		return Optional.empty();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteComments(String module, Integer moduleId) {
		CommentModuleHandler handler = handlerMap.get(module);
		if (handler != null) {
			handler.deleteComments(moduleId);
		}
	}

	/**
	 * 禁止某条评论的ip评论
	 * 
	 * @param commentId
	 * @throws LogicException
	 */
	@Transactional(readOnly = true)
	public void banIp(Integer commentId) throws LogicException {
		Comment comment = commentDao.selectById(commentId);
		if (comment != null) {
			blacklistHandler.add(comment.getIp());
		}
	}

	/**
	 * 取消禁止某个ip
	 * 
	 * @param ip
	 */
	public void removeBan(String ip) {
		blacklistHandler.remove(ip);
	}

	/**
	 * 分页查询被禁止的ip
	 * 
	 * @param param
	 * @return
	 */
	public PageResult<String> queryBlacklist(IPQueryParam param) {
		return blacklistHandler.query(param);
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (blacklistHandler == null) {
			blacklistHandler = new DefaultBlacklistHandler();
		}

		if (markdown2Html == null) {
			markdown2Html = CommonMarkdown2Html.INSTANCE;
		}

		if (htmlClean == null) {
			htmlClean = html -> Jsoup.clean(html, Whitelist.simpleText());
		}
		Resources.readResource(configResource, pros::load);
		loadConfig();
	}

	@EventListener
	void start(ContextRefreshedEvent evt) throws Exception {

		if (evt.getApplicationContext().getParent() == null) {
			Collection<CommentModuleHandler> handlers = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(evt.getApplicationContext(), CommentModuleHandler.class, true, false)
					.values();

			if (!CollectionUtils.isEmpty(handlers)) {
				for (CommentModuleHandler handler : handlers) {
					this.addCommentModuleHandler(handler);
				}
			}

			Collection<CommentChecker> checkers = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(evt.getApplicationContext(), CommentChecker.class, true, false)
					.values();
			if (!CollectionUtils.isEmpty(checkers)) {
				this.checkers.addAll(checkers);
			}
		}

	}

	private List<Comment> buildTree(List<Comment> comments) {
		CollectFilteredFilter filter = new CollectFilteredFilter(null);
		List<Comment> roots = new ArrayList<>();
		comments.stream().filter(filter).collect(Collectors.toList())
				.forEach(comment -> roots.add(pickByParent(comment, filter.rests)));
		return roots;
	}

	private Comment pickByParent(Comment parent, List<Comment> comments) {
		Objects.requireNonNull(parent);
		CollectFilteredFilter filter = new CollectFilteredFilter(parent);
		List<Comment> children = comments.stream().filter(filter).collect(Collectors.toList());
		children.forEach(child -> pickByParent(child, filter.rests));
		parent.setChildren(children);
		return parent;
	}

	private List<Comment> handleTree(List<Comment> comments, boolean asc) {
		if (comments.isEmpty()) {
			return comments;
		}
		List<Comment> tree = buildTree(comments);
		tree.sort(asc ? ascCommentComparator : descCommentComparator);
		return tree;
	}

	private void handleComment(Comment comment) {
		CommentModule module = comment.getCommentModule();
		if (module != null && module.getModule() != null && module.getId() != null) {
			comment.setUrl(urlHelper.getUrl() + "/comment/link/" + module.getModule() + "/" + module.getId());
		} else {
			comment.setUrl(urlHelper.getUrl());
		}
		Comment p = comment.getParent();
		if (p != null) {
			fillComment(p);
		}
		fillComment(comment);
	}

	private void handleCommentsContent(List<Comment> comments) {
		Map<Integer, String> htmlMap = markdown2Html
				.toHtmls(comments.stream().filter(cmt -> Editor.MD.equals(cmt.getEditor()))
						.collect(Collectors.toMap(Comment::getId, Comment::getContent)));
		comments.forEach(cmt -> {
			String content = cmt.getContent();
			if (Editor.MD.equals(cmt.getEditor())) {
				content = htmlMap.get(cmt.getId());
			}
			if (!cmt.getAdmin()) {
				cmt.setContent(htmlClean.clean(content));
			} else {
				cmt.setContent(content);
			}
		});
	}

	private void fillComment(Comment comment) {
		if (Environment.hasAuthencated()) {
			comment.setBan(blacklistHandler.match(comment.getIp()));
		}
		if (comment.getAdmin() == null || !comment.getAdmin()) {
			return;
		}
		User user = userService.getUser();
		/**
		 * @since 6.2
		 */
		comment.setNickname(config.getNickname());
		String email = user.getEmail();
		comment.setEmail(email);
		comment.setGravatar(user.getGravatar());

		if (!Environment.hasAuthencated()) {
			comment.setIp(null);
			comment.setEmail(null);
		}
	}

	private void loadConfig() {
		config = new CommentConfig();
		config.setEditor(Editor.valueOf(pros.getProperty(COMMENT_EDITOR, "MD")));
		config.setCheck(Boolean.parseBoolean(pros.getProperty(COMMENT_CHECK, "false")));
		config.setLimitCount(Integer.parseInt(pros.getProperty(COMMENT_LIMIT_COUNT, "10")));
		config.setLimitSec(Integer.parseInt(pros.getProperty(COMMENT_LIMIT_SEC, "60")));
		config.setPageSize(Integer.parseInt(pros.getProperty(COMMENT_PAGESIZE, "10")));
		config.setNickname(pros.getProperty(COMMENT_NICKNAME, "admin"));
	}

	private final class CollectFilteredFilter implements Predicate<Comment> {
		private final Comment parent;
		private final List<Comment> rests = new ArrayList<>();

		CollectFilteredFilter(Comment parent) {
			this.parent = parent;
		}

		@Override
		public boolean test(Comment t) {
			if (Objects.equals(parent, t.getParent())) {
				return true;
			}
			rests.add(t);
			return false;
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * 添加一个评论模块处理器
	 * 
	 * @param handler
	 */
	public void addCommentModuleHandler(CommentModuleHandler handler) {
		Objects.requireNonNull(handler);
		handlerMap.put(handler.getModuleName(), handler);
	}

	public void setCheckers(List<CommentChecker> checkers) {
		this.checkers = checkers;
	}

	public void setHtmlClean(HtmlClean htmlClean) {
		this.htmlClean = htmlClean;
	}

	private final class DefaultBlacklistHandler implements BlacklistHandler {
		private final StampedLock lock = new StampedLock();
		private final Path json = Constants.CONFIG_DIR.resolve("commentBlackList.json");
		private Set<String> blacklist = new LinkedHashSet<>();

		DefaultBlacklistHandler() {
			if (FileUtils.exists(json)) {
				try {
					String str = Resources.readResourceToString(new FileSystemResource(json));
					if (!Validators.isEmptyOrNull(str, false)) {
						blacklist = new HashSet<>(Jsons.readList(String[].class, str));
					}
				} catch (Exception e) {
					throw new SystemException(e.getMessage(), e);
				}
			} else {
				FileUtils.createFile(json);
			}
		}

		@Override
		public PageResult<String> query(IPQueryParam param) {
			long stamp = lock.readLock();
			try {
				int offset = param.getOffset();
				List<String> list = new ArrayList<>(blacklist);
				String ip = param.getIp();
				if (!Validators.isEmptyOrNull(ip, true)) {
					list.removeIf(ban -> !ban.contains(ip));
				}
				int size = list.size();
				if (offset >= size) {
					return new PageResult<>(param, size, new ArrayList<>());
				}
				int end = offset + param.getPageSize();
				return new PageResult<>(param, size, list.subList(offset, Math.min(end, size)));
			} finally {
				lock.unlockRead(stamp);
			}
		}

		@Override
		public void remove(String ip) {
			long stamp = lock.writeLock();
			try {
				if (blacklist.remove(ip)) {
					try (Writer writer = Files.newBufferedWriter(json)) {
						Jsons.write(blacklist, writer);
					} catch (IOException e) {
						blacklist.add(ip);
						throw new SystemException(e.getMessage(), e);
					}
				}
			} finally {
				lock.unlockWrite(stamp);
			}
		}

		@Override
		public void add(String ip) {
			long stamp = lock.writeLock();
			try {
				blacklist.add(ip);
				try (Writer writer = Files.newBufferedWriter(json)) {
					Jsons.write(blacklist, writer);
				} catch (IOException e) {
					blacklist.remove(ip);
					throw new SystemException(e.getMessage(), e);
				}
			} finally {
				lock.unlockWrite(stamp);
			}
		}

		@Override
		public boolean match(String ip) {
			long stamp = lock.tryOptimisticRead();
			boolean match = doMatch(ip);
			if (!lock.validate(stamp)) {
				stamp = lock.readLock();
				try {
					match = doMatch(ip);
				} finally {
					lock.unlockRead(stamp);
				}
			}
			return match;
		}

		private boolean doMatch(String ip) {
			if (blacklist.isEmpty()) {
				return false;
			}
			return blacklist.contains(ip);
		}
	}
}
