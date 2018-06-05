package me.qyh.blog.plugin.mail;

import java.util.Map;
import java.util.Properties;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.FilterReply;
import me.qyh.blog.core.plugin.PluginHandler;
import me.qyh.blog.core.plugin.PluginProperties;

/**
 * 只支持SMTP！！！
 * 
 * @author wwwqyhme
 *
 */
public class MailPluginHandler implements PluginHandler {

	private static final String ENABLE_KEY = "plugin.mail.enable";
	private static final String HOST_KEY = "plugin.mail.host";
	private static final String PORT_KEY = "plugin.mail.port";
	private static final String USERNAME_KEY = "plugin.mail.username";
	private static final String PASSWORD_KEY = "plugin.mail.password";
	private static final String FROM_KEY = "plugin.mail.from";
	private static final String SMTP_AUTH_KEY = "plugin.mail.smtp.auth";

	private static final String LOG_ENABLE_KEY = "plugin.mail.log.enable";
	private static final String LOG_LAYOUT_PATTERN_KEY = "plugin.mail.log.logback.layout.pattern";
	private static final String LOG_SUBJECT_PATTERN_KEY = "plugin.mail.log.logback.subjectPattern";

	private static final String[] MAIL_APPENDAR_NAMES = { "errorDailyRollingFileAppender", "consoleAppender" };

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final boolean enable = pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);
	private final boolean logEnable = pluginProperties.get(LOG_ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Map<String, String> vs = pluginProperties.gets(HOST_KEY, PORT_KEY, USERNAME_KEY, PASSWORD_KEY, FROM_KEY,
				SMTP_AUTH_KEY);
		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(vs.get(HOST_KEY));
		javaMailSenderImpl.setPassword(vs.get(PASSWORD_KEY));
		javaMailSenderImpl.setPort(Integer.parseInt(vs.get(PORT_KEY)));
		javaMailSenderImpl.setUsername(vs.get(USERNAME_KEY));

		Properties pros = new Properties();
		pros.put("mail.smtp.auth", Boolean.parseBoolean(vs.get(SMTP_AUTH_KEY)));
		pros.put("mail.from", vs.get(FROM_KEY));

		javaMailSenderImpl.setJavaMailProperties(pros);

		applicationContext.addBeanFactoryPostProcessor(new BeanDefinitionRegistryPostProcessor() {

			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0) throws BeansException {

			}

			@Override
			public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
				BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(MailSenderImpl.class)
						.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(javaMailSenderImpl)
						.getBeanDefinition();
				registry.registerBeanDefinition(MailSenderImpl.class.getName(), definition);
			}
		});
	}

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		if (logEnable) {
			ILoggerFactory factory = LoggerFactory.getILoggerFactory();

			if (factory instanceof LoggerContext) {

				LoggerContext logCtx = (LoggerContext) factory;

				MailAppendar appendar = new MailAppendar(applicationContext.getBean(MailSender.class),
						pluginProperties.get(LOG_SUBJECT_PATTERN_KEY).orElse("%logger{20} - %m"));

				PatternLayout layout = new PatternLayout();
				layout.setContext(logCtx);
				layout.setPattern(pluginProperties.get(LOG_LAYOUT_PATTERN_KEY)
						.orElse(".%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg %n"));
				layout.start();
				appendar.setLayout(layout);

				LevelFilter filter = new LevelFilter();
				filter.setContext(logCtx);
				filter.setLevel(Level.ERROR);
				filter.setOnMatch(FilterReply.ACCEPT);
				filter.setOnMismatch(FilterReply.DENY);
				filter.start();
				appendar.addFilter(filter);

				appendar.setContext(logCtx);
				appendar.start();

				Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
				if (Level.ERROR.equals(logger.getLevel())) {
					logger.addAppender(appendar);
				}

				Logger mailLogger = (Logger) LoggerFactory.getLogger(MailSenderImpl.class);
				mailLogger.setAdditive(false);
				mailLogger.setLevel(Level.ERROR);

				Appender<ILoggingEvent> mailLoggerAppendar = null;
				for (String name : MAIL_APPENDAR_NAMES) {
					mailLoggerAppendar = logger.getAppender(name);
					if (mailLoggerAppendar != null) {
						break;
					}
				}

				if (mailLoggerAppendar == null) {

					PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
					logEncoder.setContext(logCtx);
					logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
					logEncoder.start();

					FileAppender<ILoggingEvent> fileAppendar = new FileAppender<>();
					fileAppendar.setContext(logCtx);
					fileAppendar.setName("mailSenderAppendar");
					fileAppendar.setEncoder(logEncoder);
					fileAppendar.setFile(System.getProperty("user.home") + "/blog/logs/mailSendar.log");
					fileAppendar.start();

					mailLoggerAppendar = fileAppendar;
				}

				mailLogger.addAppender(mailLoggerAppendar);
			}
		}
	}

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public boolean enable() {
		return enable;
	}

}
