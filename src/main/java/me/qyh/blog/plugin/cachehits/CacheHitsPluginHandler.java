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
package me.qyh.blog.plugin.cachehits;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class CacheHitsPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pluginProperties = PluginProperties.getInstance();
	private final boolean enable = pluginProperties.get("plugin.cachehits.enable").map(Boolean::parseBoolean)
			.orElse(true);

	private static final String CACHEIP_KEY = "plugin.cachehits.cacheIp";
	private static final String MAXIPS_KEY = "plugin.cachehits.maxIps";
	private static final String FLUSHSEC_KEY = "plugin.cachehits.flushSec";

	private static final String ARTICLE_HITS_STRATEGY_BEAN_NAME = "articleHitsStrategy";
	private static final String NEWS_HITS_STRATEGY_BEAN_NAME = "newsHitsStrategy";

	private static final String ARTICLE_FLUSHNUM_KEY = "plugin.cachehits.article.flushNum";
	private static final String NEWS_FLUSHNUM_KEY = "plugin.cachehits.news.flushNum";

	@Override
	protected void registerBean(BeanRegistry registry) {
		boolean cacheIp = pluginProperties.get(CACHEIP_KEY).map(Boolean::parseBoolean).orElse(true);
		int maxIps = pluginProperties.get(MAXIPS_KEY).map(Integer::parseInt).orElse(100);
		int flushSec = pluginProperties.get(FLUSHSEC_KEY).map(Integer::parseInt).orElse(600);

		int articleFlushNum = pluginProperties.get(ARTICLE_FLUSHNUM_KEY).map(Integer::parseInt).orElse(50);
		int newsFlushNum = pluginProperties.get(NEWS_FLUSHNUM_KEY).map(Integer::parseInt).orElse(50);

		registry.register(ARTICLE_HITS_STRATEGY_BEAN_NAME,
				BeanDefinitionBuilder.genericBeanDefinition(ArticleCacheableHitsStrategy.class)
						.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(cacheIp)
						.addConstructorArgValue(maxIps).addConstructorArgValue(articleFlushNum)
						.addConstructorArgValue(flushSec).getBeanDefinition());

		registry.register(NEWS_HITS_STRATEGY_BEAN_NAME,
				BeanDefinitionBuilder.genericBeanDefinition(NewsCacheableHitsStrategy.class)
						.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(cacheIp)
						.addConstructorArgValue(maxIps).addConstructorArgValue(newsFlushNum)
						.addConstructorArgValue(flushSec).getBeanDefinition());
	}

	@Override
	public boolean enable() {
		return enable;
	}

}
