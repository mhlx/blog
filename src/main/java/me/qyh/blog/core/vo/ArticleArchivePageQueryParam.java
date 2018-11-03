package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.Space;

public class ArticleArchivePageQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean queryPrivate;// 查询私人博客
	private Space space;

	public boolean isQueryPrivate() {
		return queryPrivate;
	}

	public void setQueryPrivate(boolean queryPrivate) {
		this.queryPrivate = queryPrivate;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

}
