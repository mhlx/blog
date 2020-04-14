package me.qyh.blog.support.logback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import me.qyh.blog.Blog;
import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.service.BlogConfigService;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StringUtils;

public class EmailAppender<E> extends UnsynchronizedAppenderBase<E> {

	private JavaMailSender javaMailSender;
	private BlogConfigService configService;
	private ReentrantLock lock = new ReentrantLock();
	private Path current;

	private Encoder<E> encoder;
	private int seconds = 600;

	private ScheduledFuture<?> future;

	private int maxMByte = 10;// when error log reached this size,send email immediately
	private long maxByte;

	@Override
	public void start() {
		if (seconds < 0) {
			System.err.println("seconds must be positive!");
			return;
		}
		if (maxMByte < 0) {
			System.err.println("maxMByte must be positive!");
			return;
		}
		maxByte = maxMByte * 1024 * 1024L;
		super.start();
		future = getContext().getScheduledExecutorService().scheduleAtFixedRate(this::sendEmail, seconds, seconds,
				TimeUnit.SECONDS);
	}

	@Override
	protected void append(E event) {
		if (!isStarted()) {
			return;
		}
		if (javaMailSender == null) {
			ApplicationContext context = getApplicationContext();
			if (context == null) {
				return;
			}
			javaMailSender = context.getBeanProvider(JavaMailSender.class).getIfAvailable();
			if (javaMailSender == null) {
				this.stop();
				return;
			}
			configService = context.getBean(BlogConfigService.class);
		}
		String error = new String(this.encoder.encode(event), StandardCharsets.UTF_8);
		lock.lock();
		try {
			if (current == null || !Files.exists(current)) {
				// create temp file
				try {
					current = Files.createTempFile(null, ".log");
				} catch (IOException e) {
					this.stop();
					return;
				}
			}

			try {
				Files.writeString(current, error, StandardOpenOption.APPEND);
				if (Files.size(current) > maxByte) {
					sendAsync();
				}
			} catch (IOException e) {

			}
		} finally {
			lock.unlock();
		}
	}

	private void sendEmail() {
		if (configService == null || javaMailSender == null) {
			return;
		}
		if (current == null || !Files.exists(current)) {
			return;
		}
		lock.lock();
		try {
			sendAsync();
		} finally {
			lock.unlock();
		}
	}

	private void sendAsync() {
		try {
			final BlogConfig config = configService.getConfig();
			if (StringUtils.isNullOrBlank(config.getEmail())) {
				return;
			}
			Path copy;
			try {
				copy = Files.createTempFile(null, ".log");
				Files.move(current, copy, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return;
			}
			super.getContext().getScheduledExecutorService().execute(() -> {
				try {
					javaMailSender.send(mm -> {
						MimeMessageHelper helper = new MimeMessageHelper(mm, true, StandardCharsets.UTF_8.name());
						helper.setText("error.log", false);
						helper.setTo(configService.getConfig().getEmail());
						helper.setSubject("error.log");
						helper.addAttachment("error_log.txt", copy.toFile());
						mm.setFrom();
					});
				} finally {
					FileUtils.deleteQuietly(copy);
				}
			});
		} finally {
			FileUtils.deleteQuietly(current);
			current = null;
		}
	}

	@Override
	public void stop() {
		super.stop();
		sendEmail();
		if (future != null) {
			future.cancel(true);
		}
	}

	public void setEncoder(Encoder<E> encoder) {
		this.encoder = encoder;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public void setMaxMByte(int maxMByte) {
		this.maxMByte = maxMByte;
	}

	private ApplicationContext getApplicationContext() {
		return Blog.getApplicationContext();
	}
}
