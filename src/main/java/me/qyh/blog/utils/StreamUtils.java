package me.qyh.blog.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StreamUtils {

	private StreamUtils() {
		super();
	}

	/**
	 * 将InputStream的内容转化为文本 <b>不会关闭流</b>
	 * 
	 * @param is
	 * @return
	 */
	public static String toString(InputStream is) throws IOException {
		return new String(is.readAllBytes(), StandardCharsets.UTF_8);
	}

}
