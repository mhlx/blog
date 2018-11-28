package me.qyh.blog.core.config;

/**
 * 全局配置
 * 
 * @author mhlx
 *
 */
public class GlobalConfig {

	/**
	 * 文件管理每页数量
	 */
	private int filePageSize;

	/**
	 * 用户模板片段管理分页数量
	 */
	private int fragmentPageSize;

	/**
	 * 用户自定义页面分页数量
	 */
	private int pagePageSize;

	/**
	 * 文章页面分页数量
	 */
	private int articlePageSize;

	/**
	 * 标签页面分页数量
	 */
	private int tagPageSize;

	private int newsPageSize;

	private int articleArchivePageSize;

	public int getFilePageSize() {
		return filePageSize;
	}

	public void setFilePageSize(int filePageSize) {
		this.filePageSize = filePageSize;
	}

	public int getFragmentPageSize() {
		return fragmentPageSize;
	}

	public void setFragmentPageSize(int fragmentPageSize) {
		this.fragmentPageSize = fragmentPageSize;
	}

	public int getPagePageSize() {
		return pagePageSize;
	}

	public void setPagePageSize(int pagePageSize) {
		this.pagePageSize = pagePageSize;
	}

	public int getArticlePageSize() {
		return articlePageSize;
	}

	public void setArticlePageSize(int articlePageSize) {
		this.articlePageSize = articlePageSize;
	}

	public int getTagPageSize() {
		return tagPageSize;
	}

	public void setTagPageSize(int tagPageSize) {
		this.tagPageSize = tagPageSize;
	}

	public int getNewsPageSize() {
		return newsPageSize;
	}

	public void setNewsPageSize(int newsPageSize) {
		this.newsPageSize = newsPageSize;
	}

	public int getArticleArchivePageSize() {
		return articleArchivePageSize;
	}

	public void setArticleArchivePageSize(int articleArchivePageSize) {
		this.articleArchivePageSize = articleArchivePageSize;
	}

}
