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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMapping;

import me.qyh.blog.core.service.impl.HitsStrategy;
import me.qyh.blog.template.render.data.DataTagProcessor;
import me.qyh.blog.web.WebExceptionResolver;

/**
 * 
 * 插件处理器
 * 
 * @author wwwqyhme
 *
 */
public interface PluginHandler extends Ordered {

	/**
	 * 在<b>Root</b>ApplicationContext初始化前调用
	 * 
	 * @param applicationContext
	 */
	default void initialize(ConfigurableApplicationContext applicationContext) throws Exception {

	}

	/**
	 * 在<b>Child</b>ApplicationContext初始化前调用
	 * 
	 * @param applicationContext
	 */
	default void initializeChild(ConfigurableApplicationContext applicationContext) throws Exception {

	}

	/**
	 * 当Root ApplicationContext<b>加载完成后</b>，初始化插件时调用
	 * 
	 * @param applicationContext
	 *            child application context for name space 'blog-serlvet'
	 */
	default void init(ApplicationContext applicationContext) throws Exception {

	}

	/**
	 * 当Child ApplicationContext<b>加载完成后</b>，初始化插件时调用
	 * 
	 * @param applicationContext
	 *            child application context for name space 'blog-serlvet'
	 */
	default void initChild(ApplicationContext applicationContext) throws Exception {

	}

	/**
	 * 添加DataTagProcessor
	 * 
	 * @see DataTagProcessor
	 * @param registry
	 */
	default void addDataTagProcessor(DataTagProcessorRegistry registry) throws Exception {

	}

	/**
	 * 添加模板
	 * 
	 * @param registry
	 * @throws Exception
	 */
	default void addTemplate(TemplateRegistry registry) throws Exception {

	}

	/**
	 * 添加RequestMapping
	 * 
	 * @see RequestMapping
	 * @param registry
	 */
	default void addRequestHandlerMapping(RequestMappingRegistry registry) throws Exception {

	}

	/**
	 * 添加管理台餐单
	 * 
	 * @param registry
	 */
	default void addMenu(MenuRegistry registry) throws Exception {

	}

	/**
	 * 添加异常处理
	 * 
	 * <p>
	 * 优先级低于默认的异常处理器
	 * </p>
	 * 
	 * @see WebExceptionResolver
	 * 
	 * @param registry
	 */
	default void addExceptionHandler(ExceptionHandlerRegistry registry) throws Exception {

	}

	/**
	 * 添加文章内容处理器
	 * 
	 * @param registry
	 */
	default void addArticleContentHandler(ArticleContentHandlerRegistry registry) throws Exception {

	}

	/**
	 * 添加文件存储器
	 * 
	 * @param registry
	 */
	default void addFileStore(FileStoreRegistry registry) throws Exception {

	}

	/**
	 * 添加模板拦截器
	 * 
	 * @param registry
	 */
	default void addTemplateInterceptor(TemplateInterceptorRegistry registry) throws Exception {

	}

	/**
	 * 注册拦截器
	 * <p>
	 * <b>优先级低于系统自带的拦截器</b>
	 * </p>
	 * 
	 * @param registry
	 */
	default void addHandlerInterceptor(HandlerInterceptorRegistry registry) throws Exception {

	}

	/**
	 * 添加锁提供器
	 * 
	 * @param registry
	 */
	default void addLockProvider(LockProviderRegistry registry) throws Exception {

	}

	/**
	 * 增加文章点击处理器
	 * <p>
	 * <b>不应该在处理器内改变文章的点击数，如果需要改变点击数，可以通过{@code HitsStrategy}来处理</b>
	 * </p>
	 * 
	 * @see HitsStrategy
	 * @param hitHandler
	 * @throws Exception
	 */
	default void addHitHandler(ArticleHitHandlerRegistry registry) throws Exception {

	}

	/**
	 * 增加模板渲染时的辅助对象
	 * <p>
	 * 这个方法不会覆盖系统本来的辅助对象！
	 * </p>
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	default void addTemplateRenderModal(TemplateRenderModelRegistry registry) throws Exception {

	}

	/**
	 * 注册ResourceHttpRequestHandlerMapping
	 * 
	 * @param registry
	 * @throws Exception
	 */
	default void addResourceHttpRequestHandlerMapping(ResourceHttpRequestHandlerMappingRegistry registry)
			throws Exception {

	}

	/**
	 * 配置mybatis
	 * 
	 * @param configurer
	 */
	default void configureMybatis(MybatisConfigurer configurer) throws Exception {

	}

	/**
	 * 判断插件是否可用
	 * 
	 * @return
	 */
	default boolean enable() {
		return true;
	}

	/**
	 * 用于插件排序
	 */
	default int getOrder() {
		return 0;
	}

}
