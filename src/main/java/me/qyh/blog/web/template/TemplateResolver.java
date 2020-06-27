package me.qyh.blog.web.template;

import me.qyh.blog.entity.Template;
import me.qyh.blog.service.TemplateService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

public class TemplateResolver implements ITemplateResolver {

    private final TemplateService templateService;

    public TemplateResolver(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public String getName() {
        return "blog template resolver";
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
        ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return templateService.findTemplate(ra.getRequest(), template).
                map(tpl -> new TemplateResolution(new TemplateResource(tpl),
                        TemplateMode.HTML, NonCacheableCacheEntryValidity.INSTANCE)).orElse(null);
    }


    public static final class TemplateResource implements ITemplateResource {

        private final Template template;

        @Override
        public String getDescription() {
            return null;
        }

        private TemplateResource(Template template) {
            super();
            this.template = template;
        }

        @Override
        public String getBaseName() {
            return template.getName();
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Reader reader() throws IOException {
            return new StringReader(template.getContent());
        }

        @Override
        public ITemplateResource relative(String relativeLocation) {
            throw new TemplateInputException("not support relative template");
        }

        public Template getTemplate() {
            return template;
        }
    }
}
