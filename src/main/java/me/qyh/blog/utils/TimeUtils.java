package me.qyh.blog.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.i18n.LocaleContextHolder;

public class TimeUtils {

	private TimeUtils() {
		super();
	}

	private static final Map<String, DateTimeFormatter> cache = new ConcurrentHashMap<>();

	public static String format(Temporal temporal, String pattern) {
		if (temporal == null) {
			return "";
		}
		return getFormatter(pattern).format(temporal);
	}

	public static LocalDateTime parse(String text, String pattern) {
		return LocalDateTime.parse(text, getFormatter(pattern));
	}

	private static DateTimeFormatter getFormatter(String pattern) {
		Locale locale = LocaleContextHolder.getLocale();
		String key = locale + "|" + pattern;
		return cache.computeIfAbsent(key, k -> DateTimeFormatter.ofPattern(pattern, locale));
	}
}
