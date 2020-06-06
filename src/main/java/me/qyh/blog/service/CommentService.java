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
	public void checkComment(int id, boolean checking) {
		Comment comment = commentMapper.selectById(id)
				.orElseThrow(() -> new ResourceNotFoundException("comment.notExists", "评论不存在"));
		comment.setChecking(checking);
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
	public List<Comment> getCommentConversation(int id, CommentModule module) {
		CommentModuleHandler<?> handler = getRequiredCommentModuleHandler(module.getName());
		handler.checkBeforeQuery(module);
		Optional<Comment> opComment = commentMapper.selectById(id);
		if (opComment.isEmpty() || !opComment.get().getModule().equals(module)) {
			throw new ResourceNotFoundException("comment.notExists", "评论不存在");
		}
		Comment comment = opComment.get();
		List<Comment> parents = Arrays.stream(comment.getParentPath().split("/")).filter(Predicate.not(String::isEmpty))
				.map(Integer::parseInt).map(pid -> commentMapper.selectById(pid).orElseThrow())
				.collect(Collectors.toList());
		parents.add(comment);
		List<Comment> children = commentMapper.selectChildren(id, false);
		parents.addAll(children);
		parents.sort(Comparator.comparing(Comment::getCreateTime));
		processCommentsContentAndBaseInfo(parents);
		return parents;
	}

	@Transactional(readOnly = true)
	public Optional<Comment> getComment(int id, CommentModule module) {
		CommentModuleHandler<?> handler = getRequiredCommentModuleHandler(module.getName());
		handler.checkBeforeQuery(module);
		Optional<Comment> opComment = commentMapper.selectById(id);
		if (opComment.isPresent() && opComment.get().getModule().equals(module)) {
			Comment comment = opComment.get();
			processCommentsContentAndBaseInfo(List.of(comment));
			return opComment;
		}
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public Optional<Comment> getCommentForEdit(int id) {
		return commentMapper.selectById(id);
	}

	@Transactional(readOnly = true)
	public PageResult<Comment> queryComments(CommentQueryParam param) {
		final CommentModule module;
		if (param.getParent() != null) {
			Optional<Comment> opParent = commentMapper.selectById(param.getParent());
			if (opParent.isEmpty()) {
				throw new ResourceNotFoundException("comment.parent.notExists", "父评论不存在");
			}
			module = opParent.get().getModule();
			getRequiredCommentModuleHandler(module.getName()).checkBeforeQuery(module);
		} else {
			module = param.getModule();
			if (module == null || module.getId() == null || module.getName() == null) {
				if (!BlogContext.isAuthenticated()) {
					throw new AuthenticationException();
				}
			} else {
				getRequiredCommentModuleHandler(module.getName()).checkBeforeQuery(module);
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
		processCommentsContentAndBaseInfo(datas);
		return new PageResult<>(param, count, datas);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public SavedComment saveComment(Comment comment) {
		CommentModule module = comment.getModule();
		CommentModuleHandler<?> handler = getRequiredCommentModuleHandler(module.getName());
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
	 * <b>bad performance</b>
	 * </p>
	 * 
	 * @param num        条数
	 * @param queryAdmin 是否查询管理员
	 * @return 最近评论
	 */
	@Transactional(readOnly = true)
	public List<Comment> getLastComments(int num, boolean queryAdmin) {
		List<Comment> comments = new ArrayList<>();
		CommentQueryParam param = new CommentQueryParam();
		param.setCurrentPage(1);
		param.setPageSize(num);
		param.setChecking(false);
		param.setQueryAdmin(queryAdmin);
		Map<CommentModule, Boolean> cache = new HashMap<>();
		while (comments.size() < num) {
			List<Comment> tops = commentMapper.selectPage(param);
			for (Comment top : tops) {
				Boolean fromCache = cache.get(top.getModule());
				if (fromCache == null) {
					try {
						getCommentModuleHandler(top.getModule().getName())
								.ifPresent(h -> h.checkBeforeQuery(top.getModule()));
						cache.put(top.getModule(), Boolean.TRUE);
					} catch (AuthenticationException | LogicException e) {
						cache.put(top.getModule(), Boolean.FALSE);
						continue;
					}
				} else if (Boolean.FALSE.equals(fromCache)) {
					continue;
				}
				processCommentsBaseInfo(List.of(top));
				comments.add(top);
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

	@Transactional(readOnly = true)
	public Object getModuleTarget(CommentModule module) {
		return getRequiredCommentModuleHandler(module.getName()).checkBeforeQuery(module);
	}

	private void processCommentsBaseInfo(List<Comment> comments) {
		BlogConfig config = null;
		boolean authenticated = BlogContext.isAuthenticated();
		Map<String, Boolean> blackIpMap;
		if (authenticated) {
			blackIpMap = blackIpService.isBlackIps(comments.stream().map(Comment::getIp).collect(Collectors.toSet()));
		} else {
			blackIpMap = null;
		}
		for (Comment comment : comments) {
			Comment parent = comment.getParent();
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

	private void processCommentsContentAndBaseInfo(List<Comment> comments) {
		processCommentsContent(comments);
		processCommentsBaseInfo(comments);
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
			processCommentsContentAndBaseInfo(comments);
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
		processCommentsContentAndBaseInfo(comments);
		emailNotify(comments, null);
	}

	@EventListener(ContextClosedEvent.class)
	public void handleContextCloseEvent() {
		notifyFromQueue();
		if (this.mailSes != null && !this.mailSes.isShutdown()) {
			this.mailSes.shutdownNow();
		}
	}

	private CommentModuleHandler<?> getRequiredCommentModuleHandler(String moduleName) {
		return getCommentModuleHandler(moduleName)
				.orElseThrow(() -> new ResourceNotFoundException("commentModule.notExists", "评论模块不存在"));
	}

	private Optional<CommentModuleHandler<?>> getCommentModuleHandler(String moduleName) {
		return handlers.stream().filter(h -> h.getModuleName().equals(moduleName)).findAny();
	}

}
