package me.qyh.blog.core.util;

/**
 * 用于页面处理
 * 
 * @author mhlx
 *
 */
public class Formats {

	private Formats() {
		super();
	}

	/**
	 * 将字节转化为可读的文件大小
	 * 
	 * @param bytes
	 * @param si
	 *            如果为true，那么以1000为一个单位，否则以1024为一个单位
	 * @return
	 */
	public static String readByte(long bytes, boolean si) {
		return FileUtils.humanReadableByteCount(bytes, si);
	}

	public static String readByte(long bytes) {
		return FileUtils.humanReadableByteCount(bytes, true);
	}

}
