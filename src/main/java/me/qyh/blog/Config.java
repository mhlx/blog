package me.qyh.blog;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import me.qyh.blog.security.HtmlClean;
import me.qyh.blog.web.template.expression.BlogExpressionObjectFactory;

@Configuration
public class Config {

    @Bean
    @SuppressWarnings("unchecked")
    public ObjectMapper objectMapper(final MessageSource messageSource) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (Message.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    return new MessageSerializer((JsonSerializer<Object>) serializer, messageSource);
                }
                return super.modifySerializer(config, beanDesc, serializer);
            }
        });
        mapper.registerModule(module);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(Markdown2Html.class)
    public Markdown2Html markdown2Html(BlogProperties blogProperties, MessageSource messageSource) {
        return new DefaultMarkdown2Html(blogProperties, objectMapper(messageSource));
    }

    @Bean
    @ConditionalOnMissingBean(HtmlClean.class)
    public HtmlClean htmlClean() {
        return html -> Jsoup.clean(html, Whitelist.basicWithImages());
    }

    @Bean
    public IExpressionObjectDialect blogExpressionObjectDialect(final MessageSource messageSource,
                                                                final Markdown2Html markdown2Html) {
        return new IExpressionObjectDialect() {

            @Override
            public String getName() {
                return "blogExpressionObjectDialect";
            }

            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new BlogExpressionObjectFactory(messageSource, markdown2Html);
            }
        };
    }

}