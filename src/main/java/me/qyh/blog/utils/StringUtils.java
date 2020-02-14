package me.qyh.blog.utils;

public class StringUtils {

	private StringUtils() {
		super();
	}

	/**
	 * 判断文本是否为空或者null
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isNullOrBlank(String text) {
		return text == null || text.isBlank();
	}

}
