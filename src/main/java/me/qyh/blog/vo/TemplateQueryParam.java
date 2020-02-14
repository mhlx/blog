package me.qyh.blog.vo;

import javax.validation.constraints.Size;

public class TemplateQueryParam extends PageQueryParam {

	@Size(max = 20, message = "查询最多只能20个字符")
	private String query;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
