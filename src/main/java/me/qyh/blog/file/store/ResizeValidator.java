package me.qyh.blog.file.store;

/**
 * resize 校验器
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface ResizeValidator {
	/**
	 * 验证是否是一个正确的缩略图尺寸
	 * 
	 * @param resize
	 * @return
	 */
	boolean valid(Resize resize);
}
