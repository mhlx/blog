package me.qyh.blog;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.qyh.blog.security.HtmlClean;
import me.qyh.blog.service.TemplateService;
import me.qyh.blog.web.template.expression.BlogExpressionObjectFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(Markdown2Html.class)
    public Markdown2Html markdown2Html(BlogProperties blogProperties) {
        return new DefaultMarkdown2Html(blogProperties, objectMapper());
    }

    @Bean
    @ConditionalOnMissingBean(HtmlClean.class)
    public HtmlClean htmlClean() {
        return html -> Jsoup.clean(html, Whitelist.basicWithImages());
    }

    @Bean
    public IExpressionObjectDialect blogExpressionObjectDialect(final TemplateService templateService,
                                                                final MessageSource messageSource,
                                                                final Markdown2Html markdown2Html) {
        return new IExpressionObjectDialect() {

            @Override
            public String getName() {
                return "blogExpressionObjectDialect";
            }

            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new BlogExpressionObjectFactory(templateService, messageSource, markdown2Html);
            }
        };
    }

}