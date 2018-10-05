package me.qyh.blog.core.plugin;

import java.io.IOException;
import java.util.Arrays;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

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

	@Override
	public final void configureMybatis(MybatisConfigurer configurer) throws Exception {
		configureMybatis(new RelativeMybatisConfigurer(
				PluginHandlerRegistry.getRootPluginPackage(PluginHandlerSupport.this.getClass()) + ".", configurer));
	}

	protected void configureMybatis(RelativeMybatisConfigurer configure) throws Exception {

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

		/**
		 * 
		 * @param xmlRelativePath
		 *            相对于 me/qyh/blog/plugin/{pluginName}/ 的路径
		 * @return
		 */
		public BeanRegistry registerXml(String xmlRelativePath) {
			doAddBeanFactoryPostProcessor(registry -> {
				String rootPath = (PluginHandlerRegistry.getRootPluginPackage(PluginHandlerSupport.this.getClass())
						+ ".").replace('.', '/');
				XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
				reader.loadBeanDefinitions(new ClassPathResource(rootPath + xmlRelativePath));
			});
			return this;
		}

		/**
		 * 
		 * @param basePackages
		 *            <b>相对</>包名，例如插件名为test，那么主包名为me.qyh.plugin.test.
		 * @return
		 */
		public BeanRegistry scanAndRegister(String... relativePackages) {
			if (Validators.isEmpty(relativePackages)) {
				return this;
			}
			String rootPackage = PluginHandlerRegistry.getRootPluginPackage(PluginHandlerSupport.this.getClass()) + ".";
			doAddBeanFactoryPostProcessor(registry -> {
				ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
				String[] basePackages = Arrays.stream(relativePackages)
						.map(relativePackage -> rootPackage + relativePackage).toArray(String[]::new);
				scanner.scan(basePackages);
			});
			return this;
		}

		public BeanRegistry register(String beanName, BeanDefinition definition) {
			doAddBeanFactoryPostProcessor(registry -> registry.registerBeanDefinition(beanName, definition));
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

	/**
	 * @since 6.5
	 * @author wwwqyhme
	 *
	 */
	protected final class RelativeMybatisConfigurer {

		private final MybatisConfigurer configurer;
		private final String rootPackage;
		private final String rootPath;

		public RelativeMybatisConfigurer(String rootPackage, MybatisConfigurer configurer) {
			super();
			this.configurer = configurer;
			this.rootPackage = rootPackage;
			this.rootPath = rootPackage.replace(".", "/");
		}

		public void addBasePackages(String... relativePackages) {
			if (!Validators.isEmpty(relativePackages)) {
				String[] basePackages = Arrays.stream(relativePackages)
						.map(relativePackage -> rootPackage + relativePackage).toArray(String[]::new);
				this.configurer.addBasePackages(basePackages);
			}
		}

		public void addRelativeMapperLocations(String... mapperRelativeLocations) {
			if (!Validators.isEmpty(mapperRelativeLocations)) {
				Resource[] resources = Arrays.stream(mapperRelativeLocations)
						.map(relativePath -> new ClassPathResource(rootPath + relativePath)).toArray(Resource[]::new);
				this.configurer.addMapperLocations(resources);
			}
		}

		public void addRelativeMapperLocationPattern(String relativePattern) throws IOException {
			if (!Validators.isEmptyOrNull(relativePattern, true)) {
				ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(null);
				this.configurer.addMapperLocations(resolver.getResources(rootPath + relativePattern));
			}
		}

		public void addRelativeTypeAliasLocations(String... typeAliasRelativeResources) {
			if (!Validators.isEmpty(typeAliasRelativeResources)) {
				Resource[] resources = Arrays.stream(typeAliasRelativeResources)
						.map(relativePath -> new ClassPathResource(rootPath + relativePath)).toArray(Resource[]::new);
				this.configurer.addTypeAliasResources(resources);
			}
		}

		public void addRelativeTypeAliasLocationPattern(String relativePattern) throws IOException {
			if (!Validators.isEmptyOrNull(relativePattern, true)) {
				ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(null);
				this.configurer.addTypeAliasResources(resolver.getResources(rootPath + relativePattern));
			}
		}

	}

}
