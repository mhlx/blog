package me.qyh.blog.file;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.PathMatcher;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

@Configuration
@Conditional(value = FileCondition.class)
public class FileConfig implements WebMvcConfigurer {

	public FileConfig(WebMvcProperties mvcProperties) {
		if ("/**".equals(mvcProperties.getStaticPathPattern())) {
			throw new RuntimeException("本地文件服务已经配置，请在application.properties中指定spring.mvc.static-path-pattern，该值不能为/**");
		}
	}

	@Bean
	public SimpleUrlHandlerMapping fileMapping(FileService fileService, ResourceProperties resourceProperties,
			ContentNegotiationManager contentNegotiationManager,
			@Qualifier("mvcUrlPathHelper") UrlPathHelper urlPathHelper,
			@Qualifier("mvcPathMatcher") PathMatcher pathMatcher, WebApplicationContext context) throws Exception {
		FileResourceResolver resolver = new FileResourceResolver(fileService);
		FileResourceHttpRequestHandler handler = new FileResourceHttpRequestHandler(resolver, resourceProperties);
		handler.setApplicationContext(context);
		handler.setServletContext(context.getServletContext());
		if (urlPathHelper != null) {
			handler.setUrlPathHelper(urlPathHelper);
		}
		handler.afterPropertiesSet();
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping(Map.of("/**", handler));
		mapping.setOrder(Ordered.LOWEST_PRECEDENCE);
		mapping.setPathMatcher(pathMatcher);
		mapping.setUrlPathHelper(urlPathHelper);
		return mapping;
	}
}
