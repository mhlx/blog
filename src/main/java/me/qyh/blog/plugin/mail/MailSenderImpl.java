package me.qyh.blog.plugin.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.util.Validators;

/**
 * 邮件发送服务
 * 
 * @author Administrator
 *
 */
public class MailSenderImpl implements MailSender {

	@Autowired
	private UserService userService;
	@Autowired
	private TaskExecutor taskExecutor;

	private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderImpl.class);

	private final JavaMailSender javaMailSender;

	public MailSenderImpl(JavaMailSender javaMailSender) {
		super();
		this.javaMailSender = javaMailSender;
	}

	/**
	 * 将邮件加入发送队列
	 * 
	 * @param mb
	 *            邮件对象
	 */
	@Override
	public void send(MessageBean mb) {
		try {
			final String email = Validators.isEmptyOrNull(mb.getTo(), true) ? userService.getUser().getEmail()
					: mb.getTo();
			if (Validators.isEmptyOrNull(email, true)) {
				LOGGER.error("接受人邮箱为空，无法发送邮件");
				return;
			}
			javaMailSender.send(mimeMessage -> {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, mb.isHtml(), Constants.CHARSET.name());
				helper.setText(mb.getText(), mb.isHtml());
				helper.setTo(email);
				helper.setSubject(mb.getSubject());
				mimeMessage.setFrom();
			});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendAsync(MessageBean mb) {
		try {
			taskExecutor.execute(() -> send(mb));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
