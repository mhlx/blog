package me.qyh.blog.utils;

import java.io.ByteArrayOutputStream;
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
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(StandardCharsets.UTF_8.name());
	}

}
