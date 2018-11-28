package me.qyh.blog.template.vo;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.PageQueryParam;

public class TemplatePageQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String query;
	private Space space;

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
