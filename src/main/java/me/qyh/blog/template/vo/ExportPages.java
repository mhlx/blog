package me.qyh.blog.template.vo;

import java.util.List;

public class ExportPages {

	private Integer spaceId;
	private List<ExportPage> pages;

	public List<ExportPage> getPages() {
		return pages;
	}

	public void setPages(List<ExportPage> pages) {
		this.pages = pages;
	}

	public Integer getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Integer spaceId) {
		this.spaceId = spaceId;
	}

}
