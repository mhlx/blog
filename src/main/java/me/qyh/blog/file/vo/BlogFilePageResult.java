package me.qyh.blog.file.vo;

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.file.entity.BlogFile;

/**
 * 分页查询结果
 * 
 * @author Administrator
 *
 */
public class BlogFilePageResult {

	/**
	 * 文章路径，例如 a-&gt;b-&gt;c
	 */
	private List<BlogFile> paths = new ArrayList<>();
	private PageResult<BlogFile> page;

	public List<BlogFile> getPaths() {
		return paths;
	}

	public void setPaths(List<BlogFile> paths) {
		this.paths = paths;
	}

	public PageResult<BlogFile> getPage() {
		return page;
	}

	public void setPage(PageResult<BlogFile> page) {
		this.page = page;
	}

}
