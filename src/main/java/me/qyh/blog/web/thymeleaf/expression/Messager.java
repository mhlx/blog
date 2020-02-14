package me.qyh.blog.web.thymeleaf.expression;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;

final class Messager {
	private final MessageSource messageSource;

	public Messager(MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}

	public String getMessage(MessageSourceResolvable resolvable) {
		return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
	}

	public String getMessage(String code, String defaultMessage, Object... args) {
		return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
	}
}