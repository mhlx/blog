package me.qyh.blog.file.store;

import java.util.Arrays;

/**
 * 默认缩放校验器
 * 
 * @author Administrator
 *
 */
public class DefaultResizeValidator implements ResizeValidator {

	private Integer[] allowSizes;

	/**
	 * default
	 */
	public DefaultResizeValidator() {
		super();
	}

	/**
	 * 
	 * @param allowSizes
	 *            允许的尺寸
	 */
	public DefaultResizeValidator(Integer[] allowSizes) {
		super();
		this.allowSizes = allowSizes;
	}

	@Override
	public boolean valid(Resize resize) {
		if (resize == null) {
			return false;
		}
		if (allowSizes != null && (resize.getSize() == null || !inSize(resize.getSize()))) {
			return false;
		}
		if (resize.getSize() != null) {
			return resize.getSize() > 0;
		} else {
			if (resize.getWidth() <= 0 && resize.getHeight() <= 0) {
				return false;
			}
			// 如果没有指定纵横比但是没有指定长宽
			return resize.isKeepRatio() || (resize.getWidth() > 0 && resize.getHeight() > 0);
		}
	}

	private boolean inSize(int size) {
		return Arrays.stream(allowSizes).anyMatch(allowSize -> allowSize == size);
	}

	public void setAllowSizes(Integer[] allowSizes) {
		this.allowSizes = allowSizes;
	}
}
