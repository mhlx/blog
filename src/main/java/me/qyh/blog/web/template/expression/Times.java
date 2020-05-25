package me.qyh.blog.web.template.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import me.qyh.blog.utils.TimeUtils;

/**
 * simple datetime processor for this blog only
 * 
 * @author wwwqyhme
 *
 */
final class Times {

	public String format(Temporal temporal, String pattern) {
		return TimeUtils.format(temporal, pattern);
	}

	public LocalDateTime now() {
		return LocalDateTime.now();
	}

	public LocalDate nowDate() {
		return LocalDate.now();
	}

	public long millis() {
		return System.currentTimeMillis();
	}

	public LocalDateTime parse(String text, String pattern) {
		return TimeUtils.parse(text, pattern);
	}

	public LocalDateTime parse(String text) {
		return TimeUtils.parse(text, "yyyy-MM-dd HH:mm:ss");
	}
}
