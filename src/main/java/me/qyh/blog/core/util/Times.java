/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * java8 date utils for thymeleaf
 *
 */
public class Times {

	private static final String[] PATTERNS = { "yyyyMMdd", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm" };
	private static final DateTimeFormatterWrapper[] DATE_FORMATTERS = new DateTimeFormatterWrapper[PATTERNS.length];

	private static final LoadingCache<String, DateTimeFormatterWrapper> DATE_TIME_FORMATTER_CACHE = Caffeine
			.newBuilder().softValues().build(DateTimeFormatterWrapper::new);

	static {
		for (int i = 0; i < PATTERNS.length; i++) {
			DATE_FORMATTERS[i] = DATE_TIME_FORMATTER_CACHE.get(PATTERNS[i]);
		}
	}

	private Times() {
		super();
	}

	/**
	 * 获取现在的日期
	 * 
	 * @return
	 */
	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	/**
	 * 获取当前时间戳
	 * 
	 * @return
	 */
	public static long nowMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 解析日期
	 * 
	 * @param text
	 * @return 如果解析失败，返回null
	 */
	public static LocalDateTime parseAndGet(String text) {
		return parse(text).orElse(null);
	}

	/**
	 * 通过指定的pattern解析日期
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	public static Optional<LocalDateTime> parse(String text, String pattern) {
		return DATE_TIME_FORMATTER_CACHE.get(pattern).parse(text);
	}

	/**
	 * 解析日期
	 * 
	 * @param text
	 * @return
	 */
	public static Optional<LocalDateTime> parse(String text) {
		Objects.requireNonNull(text);
		String trim = text.strip();
		int len = trim.length();
		for (DateTimeFormatterWrapper wrapper : DATE_FORMATTERS) {
			if (wrapper.length == len) {
				return wrapper.parse(text);
			}
		}
		// 时间戳
		try {
			Long stamp = Long.parseLong(trim);
			return Optional.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(stamp), ZoneId.systemDefault()));
		} catch (DateTimeException | NumberFormatException e) {
			return Optional.empty();
		}
	}

	/**
	 * 格式化日期
	 * 
	 * @param temporal
	 *            日期
	 * @return pattern
	 */
	public static String format(Temporal temporal, String pattern) {
		Objects.requireNonNull(temporal);
		Objects.requireNonNull(pattern);
		return DATE_TIME_FORMATTER_CACHE.get(pattern).format(temporal);
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 *            日期
	 * @return pattern
	 */
	public static String format(Date date, String pattern) {
		Objects.requireNonNull(date);
		return format(toLocalDateTime(date), pattern);
	}

	/**
	 * 将Date转化为LocalDateTime
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		Objects.requireNonNull(date);
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * 解析日期
	 * 
	 * @param text
	 * @return 如果解析失败，返回null
	 */
	public static Date parseAndGetDate(String text) {
		Optional<LocalDateTime> time = parse(text);
		return time.map(Times::toDate).orElse(null);
	}

	/**
	 * 将LocalDateTime转化为date
	 * 
	 * @param time
	 * @return
	 */
	public static Date toDate(LocalDateTime time) {
		Objects.requireNonNull(time);
		return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 从日期中获取年份
	 * 
	 * @param temporal
	 * @return
	 * @see Temporal#get(ChronoField.YEAR)
	 */
	public static int getYear(Temporal temporal) {
		Objects.requireNonNull(temporal);
		return temporal.get(ChronoField.YEAR);
	}

	/**
	 * 从日期中获取月份
	 * 
	 * @param temporal
	 * @return
	 * @see Temporal#get(ChronoField.MONTH_OF_YEAR)
	 */
	public static int getMonthOfYear(Temporal temporal) {
		Objects.requireNonNull(temporal);
		return temporal.get(ChronoField.MONTH_OF_YEAR);
	}

	/**
	 * 从日期中获取是某個月的第幾天
	 * 
	 * @param temporal
	 * @return
	 * @see Temporal#get(ChronoField.MONTH_OF_YEAR)
	 */
	public static int getDayOfMonth(Temporal temporal) {
		Objects.requireNonNull(temporal);
		return temporal.get(ChronoField.DAY_OF_MONTH);
	}

	/**
	 * 从日期中获取年份
	 * 
	 * @param date
	 * @return
	 * @see #getYear(Temporal)
	 */
	public static int getYear(Date date) {
		Objects.requireNonNull(date);
		return getYear(toLocalDateTime(date));
	}

	/**
	 * 从日期中获取月份
	 * 
	 * @param date
	 * @return
	 * @see #getMonthOfYear(Temporal)
	 */
	public static int getMonthOfYear(Date date) {
		Objects.requireNonNull(date);
		return getMonthOfYear(toLocalDateTime(date));
	}

	/**
	 * 从日期中获取是某個月的第幾天
	 * 
	 * @param date
	 * @return
	 * @see #getDayOfMonth(Temporal)
	 */
	public static int getDayOfMonth(Date date) {
		Objects.requireNonNull(date);
		return getDayOfMonth(toLocalDateTime(date));
	}

	/**
	 * 获取时间戳
	 * 
	 * @param date
	 * @return
	 */
	public static long getTime(Date date) {
		Objects.requireNonNull(date);
		return date.getTime();
	}

	/**
	 * 获取时间戳
	 * 
	 * @param date
	 * @return
	 */
	public static long getTime(LocalDateTime date) {
		Objects.requireNonNull(date);
		return date.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
	}

	private static final class DateTimeFormatterWrapper {
		private final String pattern;
		private final int length;
		private final DateTimeFormatter formatter;
		private final boolean isDate;

		DateTimeFormatterWrapper(String pattern) {
			super();
			Objects.requireNonNull(pattern);
			this.pattern = pattern.strip();
			this.length = this.pattern.length();
			this.formatter = DateTimeFormatter.ofPattern(this.pattern);
			this.isDate = this.pattern.indexOf(' ') == -1;
		}

		String format(Temporal temporal) {
			return formatter.format(temporal);
		}

		Optional<LocalDateTime> parse(String text) {
			try {
				LocalDateTime time;
				if (isDate) {
					time = LocalDateTime.from(LocalDate.parse(text, formatter).atStartOfDay());
				} else {
					time = LocalDateTime.parse(text, formatter);
				}
				return Optional.of(time);
			} catch (DateTimeParseException e) {
				return Optional.empty();
			}
		}
	}
}
