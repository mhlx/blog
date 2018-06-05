package me.qyh.blog.core.plugin;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;

import me.qyh.blog.core.util.Validators;

public class PluginHandlerSupport implements PluginHandler {

	@Override
	public final void initialize(ConfigurableApplicationContext applicationContext) {
		initializeOther(applicationContext);
		registerBean(new BeanRegistry(applicationContext));
	}

	@Override
	public final void initializeChild(ConfigurableApplicationContext applicationContext) {
		initializeChildOther(applicationContext);
		registerChildBean(new BeanRegistry(applicationContext));
	}

	protected void registerBean(BeanRegistry registry) {

	}

	protected void registerChildBean(BeanRegistry registry) {

	}

	protected void initializeChildOther(ConfigurableApplicationContext applicationContext) {

	}

	protected void initializeOther(ConfigurableApplicationContext applicationContext) {

	}

	protected <T> Optional<T> getBean(Class<T> clazz, ApplicationContext ctx) {
		try {
			return Optional.of(ctx.getBean(clazz));
		} catch (BeansException e) {
			return Optional.empty();
		}
	}

	protected BeanDefinition simpleBeanDefinition(Class<?> clazz) {
		return BeanDefinitionBuilder.genericBeanDefinition(clazz).setScope(BeanDefinition.SCOPE_SINGLETON)
				.getBeanDefinition();
	}

	protected class BeanRegistry {
		private final ConfigurableApplicationContext context;

		public BeanRegistry(ConfigurableApplicationContext context) {
			super();
			this.context = context;
		}

		public BeanRegistry registerXml(Resource xml) {
			doAddBeanFactoryPostProcessor(registry -> {
				XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
				reader.loadBeanDefinitions(xml);
			});
			return this;
		}

		public BeanRegistry scanAndRegister(String... basePackages) {
			if (Validators.isEmpty(basePackages)) {
				return this;
			}
			doAddBeanFactoryPostProcessor(registry -> {
				ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
				scanner.scan(basePackages);
			});
			return this;
		}

		public BeanRegistry register(String beanName, BeanDefinition definition) {
			doAddBeanFactoryPostProcessor(registry -> {
				registry.registerBeanDefinition(beanName, definition);
			});
			return this;
		}

		private void doAddBeanFactoryPostProcessor(Consumer<BeanDefinitionRegistry> consumer) {
			context.addBeanFactoryPostProcessor(new BeanDefinitionRegistryPostProcessor() {

				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

				}

				@Override
				public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
					consumer.accept(registry);
				}
			});
		}

	}

}
