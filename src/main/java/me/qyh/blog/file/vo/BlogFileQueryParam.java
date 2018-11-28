package me.qyh.blog.file.vo;

import java.util.HashSet;
import java.util.Set;

import me.qyh.blog.core.vo.PageQueryParam;
import me.qyh.blog.file.entity.BlogFile;
import me.qyh.blog.file.entity.BlogFile.BlogFileType;

/**
 * 文件分页查询参数
 * 
 * @author Administrator
 *
 */
public class BlogFileQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer parent;
	private BlogFileType type;
	private boolean querySubDir;
	private BlogFile parentFile;
	private Set<String> extensions = new HashSet<>();
	private String name;

	/**
	 * 是否忽略分页
	 * @since 2017.11.25
	 */
	private boolean ignorePaging;

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public BlogFileType getType() {
		return type;
	}

	public void setType(BlogFileType type) {
		this.type = type;
	}

	public boolean isQuerySubDir() {
		return querySubDir;
	}

	public void setQuerySubDir(boolean querySubDir) {
		this.querySubDir = querySubDir;
	}

	public BlogFile getParentFile() {
		return parentFile;
	}

	public void setParentFile(BlogFile parentFile) {
		this.parentFile = parentFile;
	}

	public Set<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(Set<String> extensions) {
		this.extensions = extensions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

}
