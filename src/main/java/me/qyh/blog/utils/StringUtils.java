package me.qyh.blog.utils;

public class StringUtils {

	private StringUtils() {
		super();
	}

	public static boolean isNullOrBlank(String text) {
		return text == null || text.isBlank();
	}

}
