/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import me.qyh.blog.core.exception.SystemException;

public class PluginHandlerRegistry
		implements ResourceLoaderAware, ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger logger = LoggerFactory.getLogger(PluginHandlerRegistry.class);

	@Autowired
	private ArticleContentHandlerRegistry articleContentHandlerRegistry;
	@Autowired
	private FileStoreRegistry fileStoreRegistry;
	@Autowired
	private LockProviderRegistry lockProviderRegistry;
	@Autowired
	private ArticleHitHandlerRegistry articleHitHandlerRegistry;

	private ResourceLoader resourceLoader;

	private Set<String> plugins = new HashSet<>();
	private static final List<PluginHandler> handlerInstances = new ArrayList<>();

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final MybatisConfigurer mybatisConfigurer = new MybatisConfigurer();

	@EventListener
	@Order(value = Ordered.LOWEST_PRECEDENCE)
	void start(ContextRefreshedEvent evt) throws Exception {
		if (evt.getApplicationContext().getParent() == null) {
			handlerInstances.removeIf(ph -> {
				try {
					ph.init(evt.getApplicationContext());
					return false;
				} catch (Exception e) {
					logger.error("加载插件：" + getPluginName(ph.getClass()) + "失败", e);
					return true;
				}
			});
			return;
		}
		if (!handlerInstances.isEmpty()) {
			ApplicationContext applicationContext = evt.getApplicationContext();
			CountDownLatch cdl = new CountDownLatch(1);

			new Thread(() -> {

				try {
					for (PluginHandler pluginHandler : handlerInstances) {
						try {
							invokePluginHandler(pluginHandler, applicationContext);
							plugins.add(getPluginName(pluginHandler.getClass()));
						} catch (Exception e) {
							logger.error("加载插件：" + getPluginName(pluginHandler.getClass()) + "失败", e);
						}
					}
				} finally {
					cdl.countDown();
				}

			}).start();

			cdl.await();

			handlerInstances.clear();
		}
	}

	public Set<String> getPlugins() {
		return Collections.unmodifiableSet(plugins);
	}

	public static String getPluginName(Class<? extends PluginHandler> clazz) {
		String fullName = clazz.getName();
		return fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
	}

	public static String getRootPluginPackage(Class<? extends PluginHandler> clazz) {
		String fullName = clazz.getName();
		return fullName.substring(0, fullName.lastIndexOf("."));
	}

	private void invokePluginHandler(PluginHandler pluginHandler, ApplicationContext applicationContext)
			throws Exception {
		pluginHandler.initChild(applicationContext);
		pluginHandler.addDataTagProcessor(applicationContext.getBean(DataTagProcessorRegistry.class));
		pluginHandler.addTemplate(applicationContext.getBean(TemplateRegistry.class));
		pluginHandler.addRequestHandlerMapping(applicationContext.getBean(RequestMappingRegistry.class));
		pluginHandler.addExceptionHandler(applicationContext.getBean(ExceptionHandlerRegistry.class));
		pluginHandler.addArticleContentHandler(articleContentHandlerRegistry);
		pluginHandler.addMenu(MenuRegistry.getInstance());
		pluginHandler.addFileStore(fileStoreRegistry);
		pluginHandler.addTemplateInterceptor(applicationContext.getBean(TemplateInterceptorRegistry.class));
		pluginHandler.addHandlerInterceptor(applicationContext.getBean(HandlerInterceptorRegistry.class));
		pluginHandler.addLockProvider(lockProviderRegistry);
		pluginHandler.addHitHandler(articleHitHandlerRegistry);
		pluginHandler.addTemplateRenderModal(applicationContext.getBean(TemplateRenderModelRegistry.class));
		pluginHandler.addResourceHttpRequestHandlerMapping(
				applicationContext.getBean(ResourceHttpRequestHandlerMappingRegistry.class));
		pluginHandler.addTemplateRenderHandler(applicationContext.getBean(TemplateRenderHandlerRegistry.class));
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		if (applicationContext.getParent() == null) {

			initPlugins();

			sortPlugins();

			handlerInstances.removeIf(ph -> !ph.enable());

			handlerInstances.removeIf(ph -> {
				try {
					ph.configureMybatis(mybatisConfigurer);
					return false;
				} catch (Exception e) {
					logger.error("插件：" + getPluginName(ph.getClass()) + "configureMybatis失败", e);
					return true;
				}
			});

			applicationContext.addBeanFactoryPostProcessor(new BeanDefinitionRegistryPostProcessor() {

				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

				}

				@Override
				public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
					registry.registerBeanDefinition("sqlSessionFactory",
							BeanDefinitionBuilder.genericBeanDefinition(PluginSqlSessionFactoryBean.class)
									.addConstructorArgValue(mybatisConfigurer.getMapperLocations())
									.addConstructorArgValue(mybatisConfigurer.getTypeAliasResources())
									.setScope(BeanDefinition.SCOPE_SINGLETON)
									.addPropertyValue("configLocation",
											"classpath:resources/mybatis/mybatis-config.xml")
									.addPropertyReference("dataSource", "dataSource").getBeanDefinition());

					registry.registerBeanDefinition(PluginMapperScannerConfigurer.class.getName(),
							BeanDefinitionBuilder.genericBeanDefinition(PluginMapperScannerConfigurer.class)
									.addConstructorArgValue(mybatisConfigurer.getBasePackages())
									.setScope(BeanDefinition.SCOPE_SINGLETON).getBeanDefinition());

				}
			});

			handlerInstances.removeIf(ph -> {
				try {
					ph.initialize(applicationContext);
					return false;
				} catch (Exception e) {
					logger.error("插件：" + getPluginName(ph.getClass()) + "initialize失败", e);
					return true;
				}
			});

		} else {
			handlerInstances.removeIf(ph -> {
				try {
					ph.initializeChild(applicationContext);
					return false;
				} catch (Exception e) {
					logger.error("插件：" + getPluginName(ph.getClass()) + "initializeChildContext失败", e);
					return true;
				}
			});

		}
	}

	private void initPlugins() {
		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resolver);
		Resource[] resources;
		try {
			resources = resolver.getResources("classpath:me/qyh/blog/plugin/*/*PluginHandler.class");
		} catch (IOException e) {
			resources = null;
		}
		for (Resource res : resources) {
			Class<?> handlerClass;
			try {
				MetadataReader reader = metadataReaderFactory.getMetadataReader(res);
				handlerClass = Class.forName(reader.getClassMetadata().getClassName());
			} catch (ClassNotFoundException | IOException e) {
				throw new SystemException(e.getMessage(), e);
			}
			if (PluginHandler.class.isAssignableFrom(handlerClass)) {
				PluginHandler newInstance;
				try {
					newInstance = (PluginHandler) handlerClass.getConstructor().newInstance();
					handlerInstances.add(newInstance);
				} catch (Exception e) {
					logger.error("创建插件失败", e);
				}
			}
		}
	}

	private void sortPlugins() {
		handlerInstances.sort((p1, p2) -> {
			String p1Name = getPluginName(p1.getClass());
			String p2Name = getPluginName(p2.getClass());
			int order1 = pluginProperties.get("plugin.order." + p1Name).map(Integer::parseInt).orElse(p1.getOrder());
			int order2 = pluginProperties.get("plugin.order." + p2Name).map(Integer::parseInt).orElse(p2.getOrder());
			return (order1 < order2) ? -1 : (order1 > order2) ? 1 : 0;
		});

	}

}
