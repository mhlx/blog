package me.qyh.blog.core.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;

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

	private final Set<String> plugins = new HashSet<>();
	private static final List<PluginHandler> handlerInstances = new ArrayList<>();

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final MybatisConfigurer mybatisConfigurer = new MybatisConfigurer();

	/**
	 * @since 6.6
	 */
	private static final Path PLUGIN_DIR = FileUtils.HOME_DIR.resolve("blog/plugins");

	static {
		FileUtils.forceMkdir(PLUGIN_DIR);
	}

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
		return fullName.substring(fullName.lastIndexOf('.') + 1);
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
		pluginHandler.addIcon(IconRegistry.getInstance());
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
		if (!Validators.isEmpty(resources)) {
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
						addHandlerInstance(newInstance);
					} catch (Exception e) {
						logger.error("创建插件失败", e);
					}
				}
			}
		}

		// load ${user.home}/blog/plugins dir
		if (FileUtils.exists(PLUGIN_DIR)) {

			// 文件夹搜索
			List<URL> urls = new ArrayList<>();
			Set<String> pluginHandlerNames = new HashSet<>();
			FileUtils.quietlyWalk(PLUGIN_DIR, 1).filter(p -> (!p.equals(PLUGIN_DIR) && FileUtils.isDirectory(p)))
					.map(p -> {
						try {
							return p.toUri().toURL();
						} catch (MalformedURLException e) {
							throw new SystemException(e.getMessage(), e);
						}
					}).forEach(url -> {
						Optional<String> pluginHandlerOptional = findPluginHandlerNameFromDir(url);
						if (pluginHandlerOptional.isPresent()) {
							urls.add(url);
							if (!pluginHandlerNames.add(pluginHandlerOptional.get())) {
								throw new SystemException("插件:" + pluginHandlerOptional.get() + "存在重复");
							}
						}
					});
			// jar搜索
			FileUtils.quietlyWalk(PLUGIN_DIR, 1)
					.filter(p -> FileUtils.isRegularFile(p) && p.getFileName().toString().endsWith(".jar")).map(p -> {
						try {
							return p.toUri().toURL();
						} catch (MalformedURLException e) {
							throw new SystemException(e.getMessage(), e);
						}
					}).forEach(url -> {
						Optional<String> pluginHandlerOptional = findPluginHandlerNameFromJar(url);
						if (pluginHandlerOptional.isPresent()) {
							urls.add(url);
							if (!pluginHandlerNames.add(pluginHandlerOptional.get())) {
								throw new SystemException("插件:" + pluginHandlerOptional.get() + "存在重复");
							}
						}
					});

			if (!pluginHandlerNames.isEmpty()) {
				@SuppressWarnings("resource")
				// 不关闭URLClassLoader，因为如果关闭了无法再加载其他类
				URLClassLoader cl = new URLClassLoader(urls.toArray(URL[]::new),
						Thread.currentThread().getContextClassLoader());
				for (String className : pluginHandlerNames) {
					Class<?> handlerClass;
					try {
						handlerClass = cl.loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new SystemException(e.getMessage(), e);
					}
					if (PluginHandler.class.isAssignableFrom(handlerClass)) {
						PluginHandler newInstance;
						try {
							newInstance = (PluginHandler) handlerClass.getConstructor().newInstance();
							addHandlerInstance(newInstance);
						} catch (Exception e) {
							logger.error("创建插件失败", e);
						}
					}
				}
			}
		}

	}

	private Path getPluginNamePath(Path classPath) {
		Path p = classPath;
		while (!p.getParent().equals(PLUGIN_DIR)) {
			p = p.getParent();
		}
		return p;
	}

	private void sortPlugins() {
		handlerInstances.sort((p1, p2) -> {
			String p1Name = getPluginName(p1.getClass());
			String p2Name = getPluginName(p2.getClass());
			int order1 = pluginProperties.get("plugin.order." + p1Name).map(Integer::parseInt).orElse(p1.getOrder());
			int order2 = pluginProperties.get("plugin.order." + p2Name).map(Integer::parseInt).orElse(p2.getOrder());
			return Integer.compare(order1, order2);
		});
	}

	private Optional<String> findPluginHandlerNameFromDir(URL dirUrl) {
		return FileUtils.quietlyWalk(Paths.get(toURI(dirUrl)))
				.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith("PluginHandler.class"))
				.map(p -> {
					String fullClassName = getPluginNamePath(p).relativize(p).toString().replace(File.separatorChar,
							'.');
					return fullClassName.substring(0, fullClassName.length() - 6);
				}).findAny();
	}

	private Optional<String> findPluginHandlerNameFromJar(URL jarUrl) {
		try (FileSystem fs = FileSystems.newFileSystem(Paths.get(toURI(jarUrl)), null)) {
			Path root = fs.getPath("/");
			return FileUtils.quietlyWalk(root).filter(
					p -> FileUtils.isRegularFile(p) && p.getFileName().toString().endsWith("PluginHandler.class"))
					.map(p -> {
						String fullClassName = root.relativize(p).toString().replace('/', '.');
						return fullClassName.substring(0, fullClassName.length() - 6);
					}).findAny();
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	private URI toURI(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	private void addHandlerInstance(PluginHandler pluginHandler) {
		String className = pluginHandler.getClass().getName();
		for (PluginHandler p : handlerInstances) {
			if (p.getClass().getName().endsWith(className)) {
				throw new SystemException("已经存在插件" + getPluginName(pluginHandler.getClass()) + "了");
			}
		}
		handlerInstances.add(pluginHandler);
	}
}
