package me.qyh.blog.web.template;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

public class TemplateRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

	private final TemplateRequestMappingHandlerMapping mapping = TemplateRequestMappingHandlerMapping
			.getRequestMappingHandlerMapping();

	private static final Method getDataBinderFactoryMethod;
	private static final Method getModelFactoryMethod;
	private HandlerMethodArgumentResolverComposite argumentResolvers;
	private ParameterNameDiscoverer parameterNameDiscoverer;
	private static final TemplateRequestMappingHandlerAdapter adapter = new TemplateRequestMappingHandlerAdapter();

	private TemplateRequestMappingHandlerAdapter() {
		super();
	}

	static {
		getDataBinderFactoryMethod = ReflectionUtils.findMethod(RequestMappingHandlerAdapter.class,
				"getDataBinderFactory", HandlerMethod.class);
		getModelFactoryMethod = ReflectionUtils.findMethod(RequestMappingHandlerAdapter.class, "getModelFactory",
				HandlerMethod.class, WebDataBinderFactory.class);
		ReflectionUtils.makeAccessible(getDataBinderFactoryMethod);
		ReflectionUtils.makeAccessible(getModelFactoryMethod);
	}

	public Object invoke(String path, TemplateDataRequest req) throws Exception {
		HandlerMethod method = mapping.getHandlerInternal(req);
		if (method == null) {
			return null;
		}
		ServletWebRequest swr = new ServletWebRequest(req);
		WebDataBinderFactory dataBinderFactory = (WebDataBinderFactory) getDataBinderFactoryMethod.invoke(this, method);
		ModelFactory modelFactory = (ModelFactory) getModelFactoryMethod.invoke(this, method, dataBinderFactory);
		InvocableHandlerMethod invocableMethod = new InvocableHandlerMethod(method);
		if (this.argumentResolvers != null) {
			invocableMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
		}
		ModelAndViewContainer mavContainer = new ModelAndViewContainer();
		mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(req));
		modelFactory.initModel(swr, mavContainer, invocableMethod);
		invocableMethod.setDataBinderFactory(dataBinderFactory);
		invocableMethod.setParameterNameDiscoverer(parameterNameDiscoverer);
		Object result = invocableMethod.invokeForRequest(swr, mavContainer);
		if (result == null) {
			return null;
		}

		if (result instanceof ResponseEntity) {
			ResponseEntity<?> entity = (ResponseEntity<?>) result;
			return entity.getBody();
		}

		return result;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Field field = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, "argumentResolvers");
		ReflectionUtils.makeAccessible(field);
		argumentResolvers = (HandlerMethodArgumentResolverComposite) ReflectionUtils.getField(field, this);

		Field field2 = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, "parameterNameDiscoverer");
		ReflectionUtils.makeAccessible(field2);
		parameterNameDiscoverer = (ParameterNameDiscoverer) ReflectionUtils.getField(field2, this);
	}

	public static TemplateRequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
		return adapter;
	}

}
