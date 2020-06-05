package me.qyh.blog.web.template;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import me.qyh.blog.utils.WebUtils;

public class TemplateRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private static final TemplateDataHandlerMapping templateDataHandlerMapping = new TemplateDataHandlerMapping();
	private static final List<TemplateDataPattern> patterns = new ArrayList<>();

	public static HandlerMethod getTemplateDataProvider(TemplateDataRequest request) throws Exception {
		// we do not use lock
		request.removeAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		try {
			String lookupPath = request.getPath();
			request.setAttribute(LOOKUP_PATH, lookupPath);
			HandlerMethod handlerMethod = templateDataHandlerMapping.lookupHandlerMethod(lookupPath, request);
			return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
		} finally {
			ProducesRequestCondition.clearMediaTypesAttribute(request);
		}
	}

	public static List<TemplateDataPattern> getTemplateDataPatterns() {
		return List.copyOf(patterns);
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, mapping);
		if (AnnotationUtils.getAnnotation(method, TemplateDataMapping.class) != null) {
			templateDataHandlerMapping.registerHandlerMethod(handler, method, mapping);
			mapping.getPatternsCondition().getPatterns().stream().map(TemplateDataPattern::new).forEach(patterns::add);
		}
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		patterns.sort(Comparator.comparing(TemplateDataPattern::getPattern));
		templateDataHandlerMapping.setContentNegotiationManager(new ContentNegotiationManager() {

			@Override
			public List<MediaType> resolveMediaTypes(NativeWebRequest request)
					throws HttpMediaTypeNotAcceptableException {
				return List.of(MediaType.APPLICATION_JSON);
			}

		});
		templateDataHandlerMapping.setUrlPathHelper(this.getUrlPathHelper());
		templateDataHandlerMapping.setPathMatcher(this.getPathMatcher());
		templateDataHandlerMapping.setPathPrefixes(this.getPathPrefixes());
		templateDataHandlerMapping.setUseTrailingSlashMatch(this.useTrailingSlashMatch());
		templateDataHandlerMapping.setAlwaysUseFullPath(false);
	}

	@Override
	protected void initApplicationContext(ApplicationContext context) {
		super.initApplicationContext(context);
		templateDataHandlerMapping.setApplicationContext(context);
	}

	@Override
	protected void initServletContext(ServletContext servletContext) {
		super.initServletContext(servletContext);
		templateDataHandlerMapping.setServletContext(servletContext);
	}

	private static final class TemplateDataHandlerMapping extends RequestMappingHandlerMapping {

		@Override
		public void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			super.registerHandlerMethod(handler, method, mapping);
		}

		@Override
		public HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
			return super.lookupHandlerMethod(lookupPath, request);
		}

	}

	public final class TemplateDataPattern {

		private final String pattern;
		private final boolean definite;

		private TemplateDataPattern(String pattern) {
			super();
			this.pattern = pattern;
			this.definite = WebUtils.isPattern(pattern);
		}

		public String getPattern() {
			return pattern;
		}

		public boolean isDefinite() {
			return definite;
		}
	}
}
