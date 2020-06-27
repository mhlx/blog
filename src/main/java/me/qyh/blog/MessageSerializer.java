package me.qyh.blog;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Locale;

final class MessageSerializer extends JsonSerializer<Message> {

    private final JsonSerializer<Object> defaultSerializer;
    private final MessageSource messageSource;

    MessageSerializer(JsonSerializer<Object> defaultSerializer, MessageSource messageSource) {
        this.defaultSerializer = defaultSerializer;
        this.messageSource = messageSource;
    }

    /**
     * { message : 'error message', code(optional) : 'error.code', field(optional) :
     * 'error field'//form submit }
     */
    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        defaultSerializer.unwrappingSerializer(null).serialize(value, gen, provider);
        gen.writeObjectField("message", getMessage(value, LocaleContextHolder.getLocale()));
        gen.writeEndObject();
    }

    private String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        return HtmlUtils.htmlEscape(messageSource.getMessage(resolvable, locale));
    }
}