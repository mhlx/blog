package me.qyh.blog.plugin.staticfile.vo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.PageQueryParam;

/**
 * 文件分页查询参数
 * 
 * @author Administrator
 *
 */
public class StaticFileQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private Set<String> extensions = new HashSet<>();
	private String name;

	private boolean querySubDir;

	/**
	 * @since 7.0
	 */
	private boolean sortByLastModify = true;

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

	public boolean needQuery() {
		return !CollectionUtils.isEmpty(extensions) || !Validators.isEmptyOrNull(name, true);
	}

	public boolean isSortByLastModify() {
		return sortByLastModify;
	}

	public void setSortByLastModify(boolean sortByLastModify) {
		this.sortByLastModify = sortByLastModify;
	}

}
