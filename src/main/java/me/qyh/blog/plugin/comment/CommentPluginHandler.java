package me.qyh.blog.plugin.comment;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.thymeleaf.TemplateEngine;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.plugin.DataTagProcessorRegistry;
import me.qyh.blog.core.plugin.Icon;
import me.qyh.blog.core.plugin.IconRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.plugin.comment.data.CommentsDataTagProcessor;
import me.qyh.blog.plugin.comment.data.LastCommentsDataTagProcessor;
import me.qyh.blog.plugin.comment.thymeleaf.dialect.CommentDialect;

public class CommentPluginHandler extends PluginHandlerSupport {

	private static final String ENABLE_KEY = "plugin.comment.email.enable";
	private static final String LOCATION_KEY = "plugin.comment.email.templateLocation";
	private static final String SUBJECT_KEY = "plugin.comment.email.subject";
	private static final String TIPCOUNT_KEY = "plugin.comment.email.tipCount";
	private static final String PROCESS_SEND_SEC_KEY = "plugin.comment.email.processSendSec";
	private static final String FORCE_SEND_SEC_KEY = "plugin.comment.email.forceSendSec";

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private Messages messages;

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		this.messages = applicationContext.getBean(Messages.class);
	}

	@Override
	protected void registerBean(BeanRegistry registry) {

		CommentConfig cc = new CommentConfig(pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false));

		if (cc.isEnableEmailNotify()) {
			EmailNofityConfig config = new EmailNofityConfig();
			pluginProperties.get(LOCATION_KEY).ifPresent(config::setTemplateLocation);
			pluginProperties.get(SUBJECT_KEY).ifPresent(config::setMailSubject);
			pluginProperties.get(TIPCOUNT_KEY).map(Integer::parseInt).ifPresent(config::setMessageTipCount);
			pluginProperties.get(PROCESS_SEND_SEC_KEY).map(Integer::parseInt).ifPresent(config::setProcessSendSec);
			pluginProperties.get(FORCE_SEND_SEC_KEY).map(Integer::parseInt).ifPresent(config::setForceSendSec);

			registry.register(CommentEmailNotify.class.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(CommentEmailNotify.class)
							.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(config)
							.getBeanDefinition());
		}

		registry.registerXml("bean.xml");

		registry.scanAndRegister("validator", "component");
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.scanAndRegister("web.controller");
	}

	@Override
	public void configureMybatis(RelativeMybatisConfigurer configurer) throws Exception {
		configurer.addBasePackages("dao");
		configurer.addRelativeTypeAliasLocations("mapper/typeAlias.txt");
		configurer.addRelativeMapperLocationPattern("mapper/*.xml");
	}

	@Override
	public void addIcon(IconRegistry registry) {
		registry.addIcon(new Icon(new Message("plugin.comment.iconName", "评论"), "<i class=\"far fa-comment-alt\"></i>",
				"console/comment"));
	}

	@Override
	public void addDataTagProcessor(DataTagProcessorRegistry registry) {
		CommentsDataTagProcessor cdtp = new CommentsDataTagProcessor(
				messages.getMessage("plugin.comment.data.comment", "评论"), "commentPage");
		cdtp.setCallable(true);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(cdtp);
		registry.register(cdtp);

		LastCommentsDataTagProcessor lcdtp = new LastCommentsDataTagProcessor(
				messages.getMessage("plugin.comment.data.lastComment", "最近评论"), "lastComments");
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(lcdtp);
		registry.register(lcdtp);
	}

	@Override
	public void initChild(ApplicationContext applicationContext) throws Exception {
		TemplateEngine templateEngine = applicationContext.getBean(TemplateEngine.class);
		templateEngine.addDialect(new CommentDialect(applicationContext));
	}

}
