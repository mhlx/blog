package me.qyh.blog.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.entity.BlogConfig.CommentCheckStrategy;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.utils.BCrypt;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StringUtils;
import me.qyh.blog.vo.User;

@Service
public class BlogConfigService {

	private static final Path CONFIG_RES_PATH = Paths.get(System.getProperty("user.home"))
			.resolve("blog/config.properties");
	private static final EncodedResource CONFIG_RES = new EncodedResource(new FileSystemResource(CONFIG_RES_PATH),
			StandardCharsets.UTF_8);

	private static final String COMMENT_STRATEGY_KEY = "comment.check.strategy";
	private static final String LOGINNAME_KEY = "user.loginName";
	private static final String PASSWORD_KEY = "user.password";
	private static final String EMAIL_KEY = "user.email";
	private static final String NICKNAME_KEY = "user.nickname";
	private static final String GRAVATAR_KEY = "user.gravatar";

	private static final Properties PROS;

	private BlogConfig config;

	static {
		FileUtils.createFile(CONFIG_RES_PATH);
		try {
			PROS = PropertiesLoaderUtils.loadProperties(CONFIG_RES);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public BlogConfig getConfig() {
		loadConfig();
		BlogConfig config = new BlogConfig(this.config);
		config.setPassword(null);
		return config;
	}

	public BlogConfig update(BlogConfig config, String password) {
		loadConfig();
		if (this.config.getPassword() != null
				&& !BCrypt.checkpw(password, Objects.toString(this.config.getPassword(), ""))) {
			throw new LogicException("userService.update.oldPwdInvalid", "原密码错误");
		}
		save(config);
		return new BlogConfig(this.config);
	}

	public User authenticate(String name, String password) {
		loadConfig();
		if (StringUtils.isNullOrBlank(this.config.getLoginName())
				|| StringUtils.isNullOrBlank(this.config.getPassword())) {
			throw new AuthenticationException();
		}
		if (!this.config.getLoginName().equals(name) || !BCrypt.checkpw(password, this.config.getPassword())) {
			throw new AuthenticationException();
		}
		return getUser();
	}

	public User getUser() {
		loadConfig();
		User user = new User();
		if (config.getNickname() != null) {
			user.setName(config.getNickname());
		} else {
			user.setName(config.getLoginName());
		}
		user.setEmail(config.getEmail());
		user.setGravatar(config.getGravatar());
		return user;
	}

	private synchronized void save(BlogConfig config) {
		PROS.setProperty(COMMENT_STRATEGY_KEY, config.getCommentCheckStrategy().name());
		if (!StringUtils.isNullOrBlank(config.getPassword())) {
			String pwdHash = BCrypt.hashpw(config.getPassword(), BCrypt.gensalt());
			PROS.setProperty(PASSWORD_KEY, pwdHash);
		}
		PROS.setProperty(LOGINNAME_KEY, config.getLoginName());
		PROS.setProperty(NICKNAME_KEY, Objects.toString(config.getNickname(), ""));
		PROS.setProperty(EMAIL_KEY, Objects.toString(config.getEmail(), ""));
		if (!StringUtils.isNullOrBlank(config.getEmail())) {
			PROS.setProperty(GRAVATAR_KEY,
					DigestUtils.md5DigestAsHex(config.getEmail().getBytes(StandardCharsets.UTF_8)));
		}
		try (OutputStream os = new FileOutputStream(CONFIG_RES.getResource().getFile())) {
			PROS.store(os, "");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		loadConfigFromProperties();
	}

	private void loadConfig() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					loadConfigFromProperties();
				}
			}
		}
	}

	private void loadConfigFromProperties() {
		String commentCheckStrategy = PROS.getProperty(COMMENT_STRATEGY_KEY);
		String loginName = PROS.getProperty(LOGINNAME_KEY);
		String password = PROS.getProperty(PASSWORD_KEY);
		String email = PROS.getProperty(EMAIL_KEY);
		String nickname = PROS.getProperty(NICKNAME_KEY);
		String gravatar = PROS.getProperty(GRAVATAR_KEY);
		config = new BlogConfig();
		config.setCommentCheckStrategy(commentCheckStrategy == null ? CommentCheckStrategy.FIRST_COMMENT
				: CommentCheckStrategy.valueOf(commentCheckStrategy));
		config.setEmail(StringUtils.isNullOrBlank(email) ? null : email);
		config.setLoginName(loginName);
		config.setPassword(password);
		config.setNickname(StringUtils.isNullOrBlank(nickname) ? null : nickname);
		config.setGravatar(gravatar);
	}

}
