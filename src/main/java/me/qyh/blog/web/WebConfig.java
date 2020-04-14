package me.qyh.blog.web;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.MimeType;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dialect.IPreProcessorDialect;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.preprocessor.IPreProcessor;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.security.BlackIpFilter;
import me.qyh.blog.security.BlackIpService;
import me.qyh.blog.security.CageCaptchaController;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.service.TemplateService;
import me.qyh.blog.utils.WebUtils;
import me.qyh.blog.web.template.PreTemplateHandler;
import me.qyh.blog.web.template.TemplateHandlerAdapter;
import me.qyh.blog.web.template.TemplateUtils;
import me.qyh.blog.web.template.TemplateView;
import me.qyh.blog.web.template.ThymeleafEngineContextFactory;
import me.qyh.blog.web.template.tag.DataProvider;
import me.qyh.blog.web.template.tag.DataTagProcessor;
import me.qyh.blog.web.template.tag.PasswordTagProcessor;
import me.qyh.blog.web.template.tag.PrivateTagProcessor;
import me.qyh.blog.web.template.tag.RedirectTagProcessor;
import me.qyh.blog.web.template.tag.StatusTagProcessor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final BlogHandlerExceptionResolver blogHandlerExceptionResolver;
	private final BlogProperties blogProperties;

	public WebConfig(BlogHandlerExceptionResolver blogHandlerExceptionResolver, BlogProperties blogProperties) {
		super();
		this.blogHandlerExceptionResolver = blogHandlerExceptionResolver;
		this.blogProperties = blogProperties;

		// disable thymeleaf engine logger error
		// we handle it by ourself
		org.slf4j.Logger sl4jLogger = LoggerFactory.getLogger(TemplateEngine.class);
		if (sl4jLogger instanceof Logger) {
			Logger logger = (Logger) sl4jLogger;
			logger.setLevel(Level.OFF);
		}
	}

	@Bean
	SpringTemplateEngine templateEngine(ThymeleafProperties properties,
			ObjectProvider<ITemplateResolver> templateResolvers, ObjectProvider<IDialect> dialects,
			final ObjectProvider<DataProvider<?>> dataProvider,
			final ObjectProvider<ICacheManager> cacheManagerProvider,
			PlatformTransactionManager platformTransactionManager) {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setEngineContextFactory(new ThymeleafEngineContextFactory());
		engine.setEnableSpringELCompiler(properties.isEnableSpringElCompiler());
		engine.setRenderHiddenMarkersBeforeCheckboxes(properties.isRenderHiddenMarkersBeforeCheckboxes());
		cacheManagerProvider.ifAvailable(engine::setCacheManager);
		templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
		for (IDialect dialect : createDialects(dataProvider, platformTransactionManager)) {
			engine.addDialect(dialect);
		}
		dialects.orderedStream().forEach(engine::addDialect);
		return engine;
	}

	@Bean
	ThymeleafViewResolver thymeleafViewResolver(ThymeleafProperties properties, SpringTemplateEngine templateEngine) {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine);
		resolver.setCharacterEncoding(properties.getEncoding().name());
		resolver.setContentType(
				appendCharset(properties.getServlet().getContentType(), resolver.getCharacterEncoding()));
		resolver.setProducePartialOutputWhileProcessing(
				properties.getServlet().isProducePartialOutputWhileProcessing());
		resolver.setExcludedViewNames(properties.getExcludedViewNames());
		resolver.setViewNames(properties.getViewNames());
		resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 5);
		resolver.setCache(properties.isCache());
		resolver.setViewClass(TemplateView.class);
		return resolver;
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add(blogHandlerExceptionResolver);
	}

	@Bean
	public FilterRegistrationBean<BlogContextFilter> contextFilter() {
		FilterRegistrationBean<BlogContextFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new BlogContextFilter(blogProperties));
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor() {

			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
					throws Exception {
				if (WebUtils.isConsoleRequest(request) && !BlogContext.isAuthenticated()) {
					request.setAttribute(Constants.REDIRECT_URL_ATTRIBUTE,
							ServletUriComponentsBuilder.fromRequest(request).build().toString());
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return false;
				}

				request.setAttribute("blogPros", Map.of("fileEnable", blogProperties.isFileEnable(), "totpEnable",
						blogProperties.isTotpEnable()));
				return true;
			}
		}).addPathPatterns("/console/**");
	}

	@Bean
	public FilterRegistrationBean<BlackIpFilter> authencationFilter(BlackIpService blackIpService) {
		FilterRegistrationBean<BlackIpFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new BlackIpFilter(blackIpService));
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<PreviewFilter> previewRequestFilter(TemplateService templateService) {
		FilterRegistrationBean<PreviewFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new PreviewFilter(templateService));
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return registration;
	}

	@Bean
	@ConditionalOnMissingBean(CaptchaValidator.class)
	public CaptchaValidator defaultCaptchaValidator() {
		return new CageCaptchaController(blogProperties);
	}

	// override fielderror|objecterror codesresolver
	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return BlogMessageCodeResolver.INSTANCE;
	}

	@Bean
	public TemplateHandlerAdapter templateHandlerAdapter() {
		return new TemplateHandlerAdapter();
	}

	private String appendCharset(MimeType type, String charset) {
		if (type.getCharset() != null) {
			return type.toString();
		}
		LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
		parameters.put("charset", charset);
		parameters.putAll(type.getParameters());
		return new MimeType(type, parameters).toString();
	}

	private Set<IDialect> createDialects(final ObjectProvider<DataProvider<?>> provider,
			final PlatformTransactionManager platformTransactionManager) {
		IDialect preProcessDialect = new IPreProcessorDialect() {

			@Override
			public String getName() {
				return "Blog Template Pre Processor Dialect";
			}

			@Override
			public int getDialectPreProcessorPrecedence() {
				return 0;
			}

			@Override
			public Set<IPreProcessor> getPreProcessors() {
				return Set.of(new PreProcessor());
			}

		};
		IDialect dataDialect = new AbstractProcessorDialect("template dialect", "template",
				StandardDialect.PROCESSOR_PRECEDENCE) {

			@Override
			public Set<IProcessor> getProcessors(String dialectPrefix) {
				DataTagProcessor dtp = new DataTagProcessor(dialectPrefix, platformTransactionManager);
				provider.orderedStream().forEach(dtp::registerDataProvider);
				return Set.of(dtp, new StatusTagProcessor(dialectPrefix), new RedirectTagProcessor(dialectPrefix),
						new PasswordTagProcessor(dialectPrefix), new PrivateTagProcessor(dialectPrefix));
			}
		};
		return Set.of(preProcessDialect, dataDialect);
	}

	private final class PreProcessor implements IPreProcessor {

		@Override
		public TemplateMode getTemplateMode() {
			return TemplateMode.HTML;
		}

		@Override
		public int getPrecedence() {
			return 0;
		}

		@Override
		public Class<? extends ITemplateHandler> getHandlerClass() {
			return PreTemplateHandler.class;
		}

	}

	private final class PreviewFilter extends OncePerRequestFilter {
		private final TemplateService templateService;

		public PreviewFilter(TemplateService templateService) {
			super();
			this.templateService = templateService;
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
				FilterChain filterChain) throws ServletException, IOException {
			TemplateUtils.setPreviewState(request, templateService.isPreviewRequest(request));
			filterChain.doFilter(request, response);
		}

	}

}