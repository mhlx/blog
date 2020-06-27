package me.qyh.blog;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;

public class VirtualMessagePropertyWriter extends VirtualBeanPropertyWriter {

    private static final MessageSource messageSource = Blog.getApplicationContext();

    protected VirtualMessagePropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
        super(propDef, contextAnnotations, declaredType);
    }

    public VirtualMessagePropertyWriter() {
        super();
    }

    @Override
    protected Object value(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        Message message = (Message) bean;
        return HtmlUtils.htmlEscape(messageSource.getMessage(message, LocaleContextHolder.getLocale()));
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
        if (!Message.class.isAssignableFrom(declaringClass.getAnnotated())) {
            throw new UnsupportedOperationException("only support Message and it's sub class");
        }
        return new VirtualMessagePropertyWriter(propDef, declaringClass.getAnnotations(), type);
    }
}
