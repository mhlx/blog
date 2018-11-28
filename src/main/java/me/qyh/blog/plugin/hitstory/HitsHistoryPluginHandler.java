package me.qyh.blog.plugin.hitstory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.plugin.ArticleHitHandlerRegistry;
import me.qyh.blog.core.plugin.DataTagProcessorRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.plugin.TemplateRegistry;
import me.qyh.blog.core.util.Resources;

public class HitsHistoryPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final boolean enable = pluginProperties.get("plugin.hitstory.enable").map(Boolean::parseBoolean)
			.orElse(true);

	private HitsHistoryLogger logger;

	private Messages messages;

	@Override
	protected void registerBean(BeanRegistry registry) {
		int max = pluginProperties.get("plugin.hitstory.max").map(Integer::parseInt).orElse(10);
		if (max < 0) {
			max = 10;
		}
		BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(HitsHistoryLogger.class)
				.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(max).getBeanDefinition();
		registry.register(HitsHistoryLogger.class.getName(), definition);
	}

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		this.logger = applicationContext.getBean(HitsHistoryLogger.class);
		this.messages = applicationContext.getBean(Messages.class);
	}

	@Override
	public void addHitHandler(ArticleHitHandlerRegistry registry) throws Exception {
		registry.register(logger);
	}

	@Override
	public void addDataTagProcessor(DataTagProcessorRegistry registry) throws Exception {
		HitsHistoryDataTagProcessor processor = new HitsHistoryDataTagProcessor(
				messages.getMessage("plugin.hitstory.data.history", "最近被访问文章"), "recentlyViewdArticles");
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(processor);
		registry.register(processor);
	}

	@Override
	public void addTemplate(TemplateRegistry registry) throws Exception {
		registry.registerGlobalFragment(messages.getMessage("plugin.hitstory.data.history", "最近被访问文章"),
				Resources.readResourceToString(new ClassPathResource("me/qyh/blog/plugin/hitstory/history.html")),
				false);
	}

	@Override
	public boolean enable() {
		return enable;
	}

}
