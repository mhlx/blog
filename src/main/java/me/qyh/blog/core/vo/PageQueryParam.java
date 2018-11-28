package me.qyh.blog.core.vo;

import java.io.Serializable;

public class PageQueryParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		return getPageSize() * (currentPage - 1);
	}

	public final int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}
}
