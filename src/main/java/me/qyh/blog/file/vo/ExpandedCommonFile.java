package me.qyh.blog.file.vo;

import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.ThumbnailUrl;

/**
 * 拓展的common file
 * 
 * @author Administrator
 *
 */
public class ExpandedCommonFile extends CommonFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ThumbnailUrl thumbnailUrl;
	private String url;

	public ExpandedCommonFile() {
		super();
	}

	public ExpandedCommonFile(CommonFile cf) {
		super(cf);
	}

	public ThumbnailUrl getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(ThumbnailUrl thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
