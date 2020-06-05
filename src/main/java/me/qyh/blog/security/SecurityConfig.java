package me.qyh.blog.security;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

	@Bean
	public FilterRegistrationBean<BlackIpFilter> authencationFilter(BlackIpService blackIpService) {
		FilterRegistrationBean<BlackIpFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new BlackIpFilter(blackIpService));
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return registration;
	}

	@Bean
	@ConditionalOnMissingBean(CaptchaValidator.class)
	public CaptchaValidator defaultCaptchaValidator(BlogProperties blogProperties) {
		return new CaptchaController(blogProperties);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor() {

			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
					throws Exception {
				if (!BlogContext.isAuthenticated() && (handler instanceof HandlerMethod)) {
					HandlerMethod hm = (HandlerMethod) handler;
					Authenticated authenticated = AnnotationUtils.findAnnotation(hm.getMethod(), Authenticated.class);
					if (authenticated == null) {
						authenticated = AnnotationUtils.findAnnotation(hm.getBeanType(), Authenticated.class);
					}
					if (authenticated != null && authenticated.required()) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
						return false;
					}
				}
				return true;
			}
		}).addPathPatterns("/**");
	}

}
