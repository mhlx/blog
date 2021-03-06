package me.qyh.blog.utils;

import org.springframework.context.i18n.LocaleContextHolder;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        DateTimeFormatter dtf = getFormatter(pattern);
        try {
            return LocalDateTime.parse(text, dtf);
        } catch (DateTimeParseException ex) {
            try {
                LocalDate localDate = LocalDate.parse(text, dtf);
                return localDate.atStartOfDay();
            } catch (DateTimeException e) {
                throw ex;
            }
        }
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        Locale locale = LocaleContextHolder.getLocale();
        String key = locale + "|" + pattern;
        return cache.computeIfAbsent(key, k -> DateTimeFormatter.ofPattern(pattern, locale));
    }
}
