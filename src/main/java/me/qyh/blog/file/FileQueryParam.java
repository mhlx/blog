package me.qyh.blog.file;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Size;

import me.qyh.blog.vo.PageQueryParam;

public class FileQueryParam extends PageQueryParam {
	@Path(message = "非法的路径")
	private String path;
	@Size(max = 5, message = "最多支持5个后缀")
	private Set<String> extensions = new HashSet<>();
	@Size(max = 20, message = "查询的名称不能超过20个字符")
	private String name;
	private boolean querySubDir;
	private boolean sortByLastModify = true;
	private boolean ignorePaging;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public boolean isQuerySubDir() {
		return querySubDir;
	}

	public void setQuerySubDir(boolean querySubDir) {
		this.querySubDir = querySubDir;
	}

	public boolean isSortByLastModify() {
		return sortByLastModify;
	}

	public void setSortByLastModify(boolean sortByLastModify) {
		this.sortByLastModify = sortByLastModify;
	}

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

	@Override
	public String toString() {
		return super.toString() + "\nFileQueryParam [path=" + path + ", extensions=" + extensions + ", name=" + name
				+ ", querySubDir=" + querySubDir + ", sortByLastModify=" + sortByLastModify + "]";
	}

}
