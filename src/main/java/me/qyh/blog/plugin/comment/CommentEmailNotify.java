package me.qyh.blog.plugin.comment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.SerializationUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.event.CommentEvent;
import me.qyh.blog.plugin.mail.MailSender;
import me.qyh.blog.plugin.mail.MessageBean;

/**
 * 用来向管理员发送评论|回复通知邮件
 * <p>
 * <strong>删除评论不会对邮件的发送造成影响，即如果发送队列中或者待发送列表中的一条评论已经被删除，那么它将仍然被发送</strong>
 * </p>
 * 
 * @author Administrator
 *
 */
public class CommentEmailNotify implements ResourceLoaderAware, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommentEmailNotify.class);
	private ConcurrentLinkedQueue<Comment> toProcesses = new ConcurrentLinkedQueue<>();
	private List<Comment> toSend = Collections.synchronizedList(new ArrayList<>());
	private final MailTemplateEngine mailTemplateEngine = new MailTemplateEngine();

	private final Path toSendSdfile = Constants.DAT_DIR.resolve("comment-toSendSdfile.dat");
	private final Path toProcessesSdfile = Constants.DAT_DIR.resolve("comment-toProcessesSdfile.dat");

	private static final String TEMPLATE_LOCATION = "classpath:me/qyh/blog/plugin/comment/template/defaultMailTemplate.html";

	private String templateLocation;
	private String mailTemplate;
	private String mailSubject;
	private int messageTipCount;
	private int processSendSec;
	private int forceSendSec;

	@Autowired
	private MailSender mailSender;
	@Autowired
	private Messages messages;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private TaskScheduler taskScheduler;

	private ResourceLoader resourceLoader;

	public CommentEmailNotify(EmailNofityConfig config) throws Exception {
		this.mailSubject = config.getMailSubject();
		if (Validators.isEmptyOrNull(mailSubject, true)) {
			mailSubject = "您有新的评论";
		}
		this.messageTipCount = config.getMessageTipCount();
		if (messageTipCount <= 0) {
			messageTipCount = 10;
		}
		String location = config.getTemplateLocation();
		if (Validators.isEmptyOrNull(location, true)) {
			location = TEMPLATE_LOCATION;
		}
		this.templateLocation = location;
		this.processSendSec = config.getProcessSendSec();
		if (processSendSec <= 0) {
			processSendSec = 5;
		}
		this.forceSendSec = config.getForceSendSec();
		if (forceSendSec <= 0) {
			forceSendSec = 300;
		}
	}

	private void sendMail(List<Comment> comments, String to) {
		Context context = new Context();
		context.setVariable("urls", urlHelper.getUrls());
		context.setVariable("comments", comments);
		context.setVariable("messages", messages);
		String text = mailTemplateEngine.process(mailTemplate, context);
		MessageBean mb = new MessageBean(mailSubject, true, text);
		if (to != null) {
			mb.setTo(to);
		}
		mailSender.sendAsync(mb);
	}

	private final class MailTemplateEngine extends TemplateEngine {
		MailTemplateEngine() {
			setTemplateResolver(new StringTemplateResolver());
		}
	}

	private void forceSend() {
		synchronized (toSend) {
			if (!toSend.isEmpty()) {
				LOGGER.debug("待发送列表不为空，将会发送邮件，无论发送列表是否达到{}", messageTipCount);
				sendMail(toSend, null);
				toSend.clear();
			}
		}
	}

	private void processToSend() {
		synchronized (toSend) {
			int size = toSend.size();
			for (Iterator<Comment> iterator = toProcesses.iterator(); iterator.hasNext();) {
				Comment toProcess = iterator.next();
				toSend.add(toProcess);
				size++;
				iterator.remove();
				if (size >= messageTipCount) {
					LOGGER.debug("发送列表尺寸达到{}立即发送邮件通知", messageTipCount);
					sendMail(toSend, null);
					toSend.clear();
					break;
				}
			}
		}
	}

	@Async
	@TransactionalEventListener
	public void handleCommentEvent(CommentEvent evt) {
		Comment comment = evt.getComment();
		Comment parent = comment.getParent();
		// 如果在用户登录的情况下评论，一律不发送邮件
		// 如果回复了管理员
		if (!comment.getAdmin() && (parent == null || parent.getAdmin())) {
			toProcesses.add(comment);
		}
		// 如果父评论不是管理员的评论
		// 如果回复是管理员
		if (parent != null && parent.getEmail() != null && !parent.getAdmin() && comment.getAdmin()) {
			// 直接邮件通知被回复对象
			sendMail(List.of(comment), comment.getParent().getEmail());
		}
	}

	@EventListener
	void start(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() != null) {
			return;
		}
		if (FileUtils.exists(toSendSdfile)) {
			try {
				this.toSend = SerializationUtils.deserialize(toSendSdfile);
			} catch (Exception e) {
				LOGGER.warn("载入文件：" + toSendSdfile + "失败:" + e.getMessage(), e);
			} finally {
				if (!FileUtils.deleteQuietly(toSendSdfile)) {
					LOGGER.warn("删除文件:{}失败，这会导致邮件重复发送", toSendSdfile);
				}
			}
		}

		if (FileUtils.exists(toProcessesSdfile)) {
			try {
				this.toProcesses = SerializationUtils.deserialize(toProcessesSdfile);
			} catch (Exception e) {
				LOGGER.warn("载入文件：" + toProcessesSdfile + "失败:" + e.getMessage(), e);
			} finally {
				if (!FileUtils.deleteQuietly(toProcessesSdfile)) {
					LOGGER.warn("删除文件:{}失败，这会导致邮件重复发送", toProcessesSdfile);
				}
			}
		}
	}

	@EventListener
	public void handleContextClosedEvent(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() != null) {
			return;
		}
		try {
			if (!toSend.isEmpty()) {
				SerializationUtils.serialize(toSend, toSendSdfile);
			}
			if (!toProcesses.isEmpty()) {
				SerializationUtils.serialize(toProcesses, toProcessesSdfile);
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.mailTemplate = Resources.readResourceToString(resourceLoader.getResource(templateLocation));
		taskScheduler.scheduleAtFixedRate(this::forceSend, forceSendSec * 1000L);
		taskScheduler.scheduleAtFixedRate(this::processToSend, processSendSec * 1000L);
	}
}
