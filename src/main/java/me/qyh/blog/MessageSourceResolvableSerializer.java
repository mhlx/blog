package me.qyh.blog;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

final class MessageSourceResolvableSerializer extends JsonSerializer<MessageSourceResolvable> {

	private final MessageSource messageSource;

	public MessageSourceResolvableSerializer(MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}

	/**
	 * { message : 'error message', code(optional) : 'error.code', field(optional) :
	 * 'error field'//form submit }
	 */
	@Override
	public void serialize(MessageSourceResolvable value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {
		if (value instanceof FieldMessageSourceResolvable) {
			FieldMessageSourceResolvable fmsr = (FieldMessageSourceResolvable) value;
			gen.writeObject(Map.of("message", getMessage(value, LocaleContextHolder.getLocale()), "field",
					fmsr.getField(), "code", value.getCodes()[0]));
			return;
		}
		gen.writeObject(
				Map.of("message", getMessage(value, LocaleContextHolder.getLocale()), "code", value.getCodes()[0]));
	}

	private String getMessage(MessageSourceResolvable resolvable, Locale locale) {
		return HtmlUtils.htmlEscape(messageSource.getMessage(resolvable, locale));
	}
}