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
