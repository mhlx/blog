package me.qyh.blog.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StringUtils {

	private static final String[] EMPTY_ARRAY = new String[0];

	private StringUtils() {

	}

	/**
	 * <p>
	 * Gets the String that is nested in between two Strings. Only the first match
	 * is returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} open/close
	 * returns {@code null} (no match). An empty ("") open and close returns an
	 * empty string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
	 * StringUtils.substringBetween(null, *, *)          = null
	 * StringUtils.substringBetween(*, null, *)          = null
	 * StringUtils.substringBetween(*, *, null)          = null
	 * StringUtils.substringBetween("", "", "")          = ""
	 * StringUtils.substringBetween("", "", "]")         = null
	 * StringUtils.substringBetween("", "[", "]")        = null
	 * StringUtils.substringBetween("yabcz", "", "")     = ""
	 * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
	 * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
	 * </pre>
	 *
	 * @param str
	 *            the String containing the substring, may be null
	 * @param open
	 *            the String before the substring, may be null
	 * @param close
	 *            the String after the substring, may be null
	 * @return the substring, {@code null} if no match
	 */
	public static String substringBetween(final String str, final String open, final String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		final int start = str.indexOf(open);
		if (start != -1) {
			final int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Searches a String for substrings delimited by a start and end tag, returning
	 * all matching substrings in an array.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} open/close
	 * returns {@code null} (no match). An empty ("") open/close returns
	 * {@code null} (no match).
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
	 * StringUtils.substringsBetween(null, *, *)            = null
	 * StringUtils.substringsBetween(*, null, *)            = null
	 * StringUtils.substringsBetween(*, *, null)            = null
	 * StringUtils.substringsBetween("", "[", "]")          = []
	 * </pre>
	 *
	 * @param str
	 *            the String containing the substrings, null returns null, empty
	 *            returns empty
	 * @param open
	 *            the String identifying the start of the substring, empty returns
	 *            null
	 * @param close
	 *            the String identifying the end of the substring, empty returns
	 *            empty array
	 * @return a String Array of substrings, or {@code new String[0]} if no match
	 */
	public static String[] substringsBetween(final String str, final String open, final String close) {
		if (str == null || Validators.isEmptyOrNull(open, true) || Validators.isEmptyOrNull(close, true)) {
			return EMPTY_ARRAY;
		}
		final int strLen = str.length();
		if (strLen == 0) {
			return EMPTY_ARRAY;
		}
		final int closeLen = close.length();
		final int openLen = open.length();
		final List<String> list = new ArrayList<>();
		int pos = 0;
		while (pos < strLen - closeLen) {
			int start = str.indexOf(open, pos);
			if (start < 0) {
				break;
			}
			start += openLen;
			final int end = str.indexOf(close, start);
			if (end < 0) {
				break;
			}
			list.add(str.substring(start, end));
			pos = end + closeLen;
		}
		if (list.isEmpty()) {
			return EMPTY_ARRAY;
		}
		return list.toArray(String[]::new);
	}

	/**
	 * 获取uuid字符串
	 * 
	 * @return uuid
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
