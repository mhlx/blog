package me.qyh.blog.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Markdown2Html;
import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.entity.BlogConfig.CommentCheckStrategy;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CommentMapper;
import me.qyh.blog.security.BlackIpService;
import me.qyh.blog.security.HtmlClean;
import me.qyh.blog.utils.StreamUtils;
import me.qyh.blog.vo.CommentQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.SavedComment;

@Component
public class CommentService {

	private final CommentMapper commentMapper;
	private final List<CommentModuleHandler<?>> handlers;
	private final CommentContentChecker contentChecker;
	private final Markdown2Html markdown2Html;
	private final BlogConfigService configService;
	private final HtmlClean htmlClean;
	private final JavaMailSender javaMailSender;
	private final BlogProperties blogProperties;
	private final MessageSource messageSource;
	private final BlackIpService blackIpService;

	private final ConcurrentLinkedQueue<Comment> notifyQueue = new ConcurrentLinkedQueue<>();

	private TemplateEngine mailTemplateEngine;
	private String mailTemplate;
	private ScheduledExecutorService mailSes;

	public CommentService(CommentMapper commentMapper, ObjectProvider<CommentModuleHandler<?>> provider,
			ObjectProvider<CommentContentChecker> contentCheckerProvider, Markdown2Html markdown2Html,
			BlogConfigService configService, HtmlClean htmlClean, ObjectProvider<JavaMailSender> mailSenderProvider,
			BlogProperties blogProperties, MessageSource messageSource, ResourceLoader resourceLoader,
			BlackIpService blackIpService, ObjectProvider<IExpressionObjectDialect> dialectsProvider)
			throws IOException {
		super();
		this.commentMapper = commentMapper;
		this.handlers = provider.orderedStream().collect(Collectors.toList());
		this.contentChecker = contentCheckerProvider.getIfAvailable();
		this.markdown2Html = markdown2Html;
		this.configService = configService;
		this.htmlClean = htmlClean;
		this.javaMailSender = mailSenderProvider.getIfAvailable();
		this.blogProperties = blogProperties;
		this.messageSource = messageSource;
		this.blackIpService = blackIpService;
		if (this.javaMailSender != null) {
			this.mailSes = Executors.newSingleThreadScheduledExecutor();
			this.mailTemplateEngine = new SpringTemplateEngine();// avoid ognl miss error
			dialectsProvider.orderedStream().forEach(mailTemplateEngine::addDialect);
			this.mailTemplateEngine.setTemplateResolver(new StringTemplateResolver());
			String templateLocation = blogProperties.getCommentEmailTemplateLocation();
			if (templateLocation == null) {
				templateLocation = "classpath:defaultTemplates/comment_mail_template.html";
			}
			this.mailTemplate = StreamUtils.toString(resourceLoader.getResource(templateLocation).getInputStream());
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteComment(int id) {
		commentMapper.deleteChildren(id);
		commentMapper.deleteById(id);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void checkComment(int id) {
		Comment comment = commentMapper.selectById(id)
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
		comment.setChecking(false);
		comment.setModifyTime(LocalDateTime.now());
		commentMapper.update(comment);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateContent(int id, String content) {
		Comment comment = commentMapper.selectById(id)
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
		if (!comment.getAdmin()) {
			throw new LogicException("comment.canNotEdit", "评论无法编辑");
		}
		comment.setContent(content);
		comment.setModifyTime(LocalDateTime.now());
		commentMapper.update(comment);
	}

	@Transactional(readOnly = true)
	public List<Comment> getCommentConversation(int id) {
		Optional<Comment> opComment = commentMapper.selectById(id);
		if (opComment.isEmpty()) {
			throw new ResourceNotFoundException("comment.notExists", "评论不存在");
		}
		Comment comment = opComment.get();
		CommentModule module = comment.getModule();
		CommentModuleHandler<?> handler = handlers.stream().filter(h -> h.getModuleName().equals(module.getName()))
				.findAny().orElseThrow();
		Object target = handler.checkBeforeQuery(module);
		List<Comment> parents = Arrays.stream(comment.getParentPath().split("/")).filter(Predicate.not(String::isEmpty))
				.map(Integer::parseInt).map(pid -> commentMapper.selectById(pid).orElseThrow())
				.collect(Collectors.toList());
		parents.add(comment);
		List<Comment> children = commentMapper.selectChildren(id, false);
		parents.addAll(children);
		parents.sort(Comparator.comparing(Comment::getCreateTime));
		processCommentsContentAndBaseInfo(parents, target);
		return parents;
	}

	@Transactional(readOnly = true)
	public Optional<Comment> getComment(int id) {
		Optional<Comment> opComment = commentMapper.selectById(id);
		if (opComment.isPresent()) {
			Comment comment = opComment.get();
			CommentModule module = comment.getModule();
			CommentModuleHandler<?> handler = handlers.stream().filter(h -> h.getModuleName().equals(module.getName()))
					.findAny().orElseThrow();
			Object target = handler.checkBeforeQuery(module);
			processCommentsBaseInfo(List.of(comment), target);
			return opComment;
		}
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public PageResult<Comment> queryComments(CommentQueryParam param) {
		final Object target;
		final CommentModule module;
		if (param.getParent() != null) {
			Optional<Comment> opParent = commentMapper.selectById(param.getParent());
			if (opParent.isEmpty()) {
				throw new ResourceNotFoundException("comment.notExists", "评论不存在");
			}
			module = opParent.get().getModule();
			Optional<CommentModuleHandler<?>> opHandler = handlers.stream()
					.filter(h -> h.getModuleName().equals(module.getName())).findAny();
			if (opHandler.isEmpty()) {
				return new PageResult<Comment>(param, 0, List.of());
			}
			target = opHandler.get().checkBeforeQuery(module);
		} else {
			module = param.getModule();
			if (module == null || module.getId() == null || module.getName() == null) {
				if (!BlogContext.isAuthenticated()) {
					throw new AuthenticationException();
				}
				target = null;
			} else {
				Optional<CommentModuleHandler<?>> opHandler = handlers.stream()
						.filter(h -> h.getModuleName().equals(module.getName())).findAny();
				if (opHandler.isEmpty()) {
					return new PageResult<Comment>(param, 0, List.of());
				}
				target = opHandler.get().checkBeforeQuery(module);
			}
		}

		if (!BlogContext.isAuthenticated()) {
			param.setChecking(false);
		}

		int count = commentMapper.selectCount(param);
		if (count == 0) {
			return new PageResult<>(param, 0, List.of());
		}

		if (param.getContain() != null) {
			Integer rank = commentMapper.selectRank(param);
			if (rank != null) {
				param.setCurrentPage(
						rank % param.getPageSize() == 0 ? rank / param.getPageSize() : rank / param.getPageSize() + 1);
			} else {
				return new PageResult<>(param, 0, List.of());
			}
		}

		List<Comment> datas = commentMapper.selectPage(param);
		if (target != null) {
			processCommentsContentAndBaseInfo(datas, target);
		} else {
			Map<CommentModule, Object> cache = new HashMap<>();
			for (Comment comment : datas) {
				Object r = cache.get(comment.getModule());
				if (r == null) {
					Optional<CommentModuleHandler<?>> opHandler = handlers.stream()
							.filter(h -> h.getModuleName().equals(comment.getModule().getName())).findAny();
					if (opHandler.isEmpty()) {
						continue;
					}
					r = opHandler.get().checkBeforeQuery(comment.getModule());
					cache.put(comment.getModule(), r);
				}
				processCommentsBaseInfo(List.of(comment), r);
			}
			processCommentsContent(datas);
		}
		return new PageResult<>(param, count, datas);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public SavedComment saveComment(Comment comment) {
		CommentModule module = comment.getModule();
		CommentModuleHandler<?> handler = handlers.stream().filter(h -> h.getModuleName().equals(module.getName()))
				.findAny().orElseThrow(() -> new ResourceNotFoundException("comment.module.notExists", "评论模块不存在"));
		handler.checkBeforeSave(comment, module);
		String content = comment.getContent();
		if (contentChecker != null) {
			contentChecker.check(content);
		}
		Comment parent = null;
		String parentPath = "/";
		if (comment.getParent() != null && comment.getParent().getId() != null) {
			parent = commentMapper.selectById(comment.getParent().getId())
					.orElseThrow(() -> new LogicException("comment.parent.notExists", "父评论不存在"));

			if (!parent.getModule().equals(module)) {
				throw new LogicException("comment.parent.mismatch", "评论模块不匹配");
			}

			if (parent.getChecking()) {
				throw new LogicException("comment.parent.checking", "无法回复正在审核的评论");
			}

			parentPath = parent.getParentPath() + parent.getId() + "/";
		}
		comment.setParentPath(parentPath);

		Optional<Comment> opLast = commentMapper.selectLastByModuleAndIp(module, comment.getIp());
		if (opLast.isPresent() && Objects.equals(opLast.get().getContent(), comment.getContent())) {
			throw new LogicException("comment.content.duplicate", "请勿评论相同的内容");
		}

		if (BlogContext.isAuthenticated()) {
			comment.setAdmin(true);
			comment.setChecking(false);
			comment.setNickname(null);
			comment.setWebsite(null);
		} else {
			String email = comment.getEmail();
			if (email != null) {
				comment.setGravatar(DigestUtils.md5DigestAsHex(email.getBytes(StandardCharsets.UTF_8)));
			}
			comment.setAdmin(false);
			BlogConfig config = configService.getConfig();
			CommentCheckStrategy ccs = config.getCommentCheckStrategy();
			if (ccs == null) {
				ccs = CommentCheckStrategy.FIRST_COMMENT;
			}
			boolean checking;
			switch (ccs) {
			case ALWALYS:
				checking = true;
				break;
			case NEVER:
				checking = false;
				break;
			case FIRST_COMMENT:
				checking = !commentMapper.isIpCommentsAnyChecked(comment.getIp());
				break;
			default:
				throw new RuntimeException("invalid comment check strategy:" + ccs);
			}
			comment.setChecking(checking);
		}
		comment.setCreateTime(LocalDateTime.now());
		commentMapper.insert(comment);
		// send email
		final Comment _parent = parent;
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				comment.setParent(_parent);
				emailNotify(comment);
			}
		});
		return new SavedComment(comment.getId(), comment.getChecking());
	}

	/**
	 * query top n comments
	 * <p>
	 * <b>bad performance because of (1+n)*k sql</b>
	 * </p>
	 * 
	 * @param num        条数
	 * @param queryAdmin 是否查询管理员
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Comment> getLastComments(int num, boolean queryAdmin) {
		List<Comment> comments = new ArrayList<>();
		CommentQueryParam param = new CommentQueryParam();
		param.setCurrentPage(1);
		param.setPageSize(num);
		param.setChecking(false);
		param.setQueryAdmin(queryAdmin);
		Map<CommentModule, Object> cache = new HashMap<>();
		while (comments.size() < num) {
			List<Comment> tops = commentMapper.selectPage(param);
			for (Comment top : tops) {
				Object target = cache.get(top.getModule());
				if (target == null) {
					Optional<CommentModuleHandler<?>> opCmh = handlers.stream()
							.filter(h -> h.getModuleName().equals(top.getModule().getName())).findAny();
					if (opCmh.isPresent()) {
						try {
							target = opCmh.get().checkBeforeQuery(top.getModule());
							Objects.requireNonNull(target);
						} catch (AuthenticationException | LogicException e) {
							continue;
						}
					} else {
						continue;
					}
					cache.put(top.getModule(), target);
				}
				processCommentsBaseInfo(List.of(top), target);
				comments.add(top);
				cache.put(top.getModule(), target);
				if (comments.size() == num) {
					break;
				}
			}
			if (tops.size() < num) {
				break;
			}
			param.setCurrentPage(param.getCurrentPage() + 1);
		}
		processCommentsContent(comments);
		return comments;
	}

	private void processCommentsBaseInfo(List<Comment> comments, Object target) {
		BlogConfig config = null;
		boolean authenticated = BlogContext.isAuthenticated();
		Map<String, Boolean> blackIpMap;
		if (authenticated) {
			blackIpMap = blackIpService.isBlackIps(comments.stream().map(Comment::getIp).collect(Collectors.toSet()));
		} else {
			blackIpMap = null;
		}
		for (Comment comment : comments) {
			comment.setTarget(target);
			Comment parent = comment.getParent();
			if (parent != null) {
				parent.setTarget(target);
			}
			if (!authenticated) {
				comment.setIp(null);
				comment.setEmail(null);
				if (parent != null) {
					parent.setId(null);
					parent.setEmail(null);
				}
			} else {
				comment.setBlackIp(blackIpMap.get(comment.getIp()));
			}
			if (comment.getAdmin()) {
				if (config == null) {
					config = configService.getConfig();
				}
				comment.setEmail(config.getEmail());
				comment.setGravatar(config.getGravatar());
				comment.setNickname(config.getNickname());
				if (comment.getNickname() == null) {
					comment.setNickname("Administrator");
				}
			}
			if (parent != null && parent.getAdmin()) {
				if (config == null) {
					config = configService.getConfig();
				}
				parent.setGravatar(config.getGravatar());
				parent.setNickname(config.getNickname());
				if (parent.getNickname() == null) {
					parent.setNickname("Administrator");
				}
			}
		}

	}

	private void processCommentsContentAndBaseInfo(List<Comment> comments, Object target) {
		processCommentsContent(comments);
		processCommentsBaseInfo(comments, target);
	}

	private void processCommentsContent(List<Comment> comments) {
		Map<Integer, String> markdownMap = markdown2Html.toHtmls(comments.stream().filter(m -> m.getContent() != null)
				.collect(Collectors.toMap(Comment::getId, Comment::getContent)));
		for (Comment comment : comments) {
			String html = markdownMap.get(comment.getId());
			comment.setContent(comment.getAdmin() ? html : htmlClean.clean(html));
		}
	}

	private void emailNotify(Comment comment) {
		if (mailSes == null) {
			return;
		}
		BlogConfig config = configService.getConfig();
		if (config.getEmail() == null) {
			return;
		}
		Comment parent = comment.getParent();
		if (!comment.getAdmin() && (parent == null || parent.getAdmin())) {
			comment.setParent(parent);
			notifyQueue.offer(comment);
		} else if (parent != null && parent.getEmail() != null && !parent.getAdmin() && comment.getAdmin()) {
			if (config.getEmail().equals(parent.getEmail())) {
				return;
			}
			List<Comment> comments = List.of(comment);
			processCommentsContentAndBaseInfo(comments, null);
			emailNotify(comments, parent.getEmail());
		}
	}

	private void emailNotify(List<Comment> comments, String to) {
		final String toAddress = to == null ? configService.getConfig().getEmail() : to;
		if (toAddress == null) {
			return;
		}
		mailSes.execute(() -> {
			Context context = new Context();
			context.setVariable("comments", comments);
			final String subject = messageSource.getMessage("comment.email.subject", null, "评论通知",
					LocaleContextHolder.getLocale());
			final String text = mailTemplateEngine.process(mailTemplate, context);
			this.javaMailSender.send(mm -> {
				MimeMessageHelper helper = new MimeMessageHelper(mm, true, StandardCharsets.UTF_8.name());
				helper.setText(text, true);
				helper.setTo(toAddress);
				helper.setSubject(subject);
				mm.setFrom();
			});
		});
	}

	@PostConstruct
	public void afterPropertiesSet() {
		if (this.mailSes != null) {
			int second = this.blogProperties.getCommentEmailNotifySecond();
			this.mailSes.scheduleWithFixedDelay(this::notifyFromQueue, second, second, TimeUnit.SECONDS);
		}
	}

	private void notifyFromQueue() {
		List<Comment> comments = new ArrayList<>();
		Comment comment;
		while ((comment = notifyQueue.poll()) != null) {
			comments.add(comment);
		}
		if (comments.isEmpty()) {
			return;
		}
		comments.sort(Comparator.comparing(Comment::getCreateTime));
		processCommentsContentAndBaseInfo(comments, null);
		emailNotify(comments, null);
	}

	@EventListener(ContextClosedEvent.class)
	public void handleContextCloseEvent() {
		notifyFromQueue();
		if (this.mailSes != null && !this.mailSes.isShutdown()) {
			this.mailSes.shutdownNow();
		}
	}

}
