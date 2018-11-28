package me.qyh.blog.plugin.staticfile.vo;

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.core.vo.PageResult;

/**
 * 分页查询结果
 * 
 * @author Administrator
 *
 */
public class StaticFilePageResult {

	/**
	 * 文章路径，例如 a-&gt;b-&gt;c
	 */
	private List<StaticFile> paths = new ArrayList<>();
	private PageResult<StaticFile> page;
	

	public StaticFilePageResult() {
		super();
	}

	public StaticFilePageResult(List<StaticFile> paths, PageResult<StaticFile> page) {
		super();
		this.paths = paths;
		this.page = page;
	}

	public List<StaticFile> getPaths() {
		return paths;
	}

	public void setPaths(List<StaticFile> paths) {
		this.paths = paths;
	}

	public PageResult<StaticFile> getPage() {
		return page;
	}

	public void setPage(PageResult<StaticFile> page) {
		this.page = page;
	}

}
