package me.qyh.blog.web.template;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class TemplateRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private static final TemplateRequestMappingHandlerMapping mapping = new TemplateRequestMappingHandlerMapping();

	private TemplateRequestMappingHandlerMapping() {
		super();
	}

	public HandlerMethod getHandlerInternal(TemplateDataRequest request) throws Exception {
		return super.getHandlerInternal(request);
	}

	public static TemplateRequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return mapping;
	}

}
