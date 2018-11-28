package me.qyh.blog.file.store;

import java.io.Serializable;

/**
 * 缩略图链接
 * 
 * @author Administrator
 *
 */
public abstract class ThumbnailUrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String small;
	private final String middle;
	private final String large;

	/**
	 * @param small
	 *            小尺寸缩略图链接
	 * @param middle
	 *            中尺寸缩略图链接
	 * @param large
	 *            大尺寸缩略图链接
	 */
	protected ThumbnailUrl(String small, String middle, String large) {
		super();
		this.small = small;
		this.middle = middle;
		this.large = large;
	}

	public String getSmall() {
		return small;
	}

	public String getMiddle() {
		return middle;
	}

	public String getLarge() {
		return large;
	}

	/**
	 * 获取缩放链接
	 * 
	 * @param width
	 * @param height
	 * @param keepRatio
	 * @return
	 */
	public abstract String getThumbUrl(int width, int height, boolean keepRatio);

	public abstract String getThumbUrl(int size);

}
