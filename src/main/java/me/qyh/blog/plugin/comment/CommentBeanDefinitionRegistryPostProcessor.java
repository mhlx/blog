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
package me.qyh.blog.plugin.comment;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import me.qyh.blog.core.config.Constants;

public class CommentBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private final CommentConfig config;

	CommentBeanDefinitionRegistryPostProcessor(CommentConfig config) {
		super();
		this.config = config;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		//
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

		if (config.isEnableEmailNotify()) {
			BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(CommentEmailNotify.class)
					.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(config.getConfig())
					.getBeanDefinition();
			registry.registerBeanDefinition(CommentEmailNotify.class.getName(), definition);
		}

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
		reader.loadBeanDefinitions(
				new EncodedResource(new ClassPathResource("me/qyh/blog/plugin/comment/bean.xml"), Constants.CHARSET));
	}

}
