package me.qyh.blog.vo;

import me.qyh.blog.BlogContext;
import me.qyh.blog.Constants;

/**
 * 分页查询
 * 
 * @author wwwqyhme
 *
 */
public class PageQueryParam {
	private int currentPage;
	private int pageSize;

	public PageQueryParam() {
		super();
	}

	public PageQueryParam(PageQueryParam param) {
		this.currentPage = param.currentPage;
		this.pageSize = param.pageSize;
	}

	public PageQueryParam(int currentPage, int pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}

	public final int getOffset() {
		return getPageSize() * (getCurrentPage() - 1);
	}

	public int getPageSize() {
		return BlogContext.isAuthenticated() ? Math.max(1, pageSize)
				: Math.min(Math.max(1, pageSize), Constants.MAX_PAGE_SIZE);
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getCurrentPage() {
		return Math.max(1, currentPage);
	}

	@Override
	public String toString() {
		return "PageQueryParam [currentPage=" + currentPage + ", pageSize=" + pageSize + "]";
	}

}
