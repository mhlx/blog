package me.qyh.blog.template.render.thymeleaf;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.template.Template;
import me.qyh.blog.template.service.TemplateService;

public class ThymeleafTemplateResolver implements ITemplateResolver {

	@Autowired
	private TemplateService templateService;

	private final ITemplateResource emptyTemplateResource = new StringTemplateResource("");

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public Integer getOrder() {
		return 1;
	}

	@Override
	public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate,
			String templateName, Map<String, Object> templateResolutionAttributes) {
		if (!Template.isTemplate(templateName)) {
			return null;
		}
		Optional<Template> optional = templateService.queryTemplate(templateName);
		ITemplateResource templateResource = optional.<ITemplateResource>map(TemplateResource::new)
				.orElse(emptyTemplateResource);
		ICacheEntryValidity cacheEntryValidity = optional
				.map(template -> template.cacheable() ? AlwaysValidCacheEntryValidity.INSTANCE
						: NonCacheableCacheEntryValidity.INSTANCE)
				.orElse(NonCacheableCacheEntryValidity.INSTANCE);

		return new TemplateResolution(templateResource, false, TemplateMode.HTML, false, cacheEntryValidity);
	}

	public final class TemplateResource implements ITemplateResource {

		private final Template template;

		private TemplateResource(Template template) {
			super();
			this.template = template;
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public String getBaseName() {
			return null;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public Reader reader() throws IOException {
			return new StringReader(template.getTemplate());
		}

		@Override
		public ITemplateResource relative(String relativeLocation) {
			throw new SystemException("unsupport");
		}

		public Template getTemplate() {
			return template;
		}
	}
}
