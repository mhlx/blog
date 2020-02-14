package me.qyh.blog.vo;

import me.qyh.blog.entity.CommentModule;

public class CommentQueryParam extends PageQueryParam {

	private Integer parent;
	private Boolean checking;
	private CommentModule module;
	private Integer contain;// should contain a comment that has this id
	private boolean asc;
	private boolean queryAdmin = true;

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public Boolean getChecking() {
		return checking;
	}

	public void setChecking(Boolean checking) {
		this.checking = checking;
	}

	public CommentModule getModule() {
		return module;
	}

	public void setModule(CommentModule module) {
		this.module = module;
	}

	public Integer getContain() {
		return contain;
	}

	public void setContain(Integer contain) {
		this.contain = contain;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public boolean isQueryAdmin() {
		return queryAdmin;
	}

	public void setQueryAdmin(boolean queryAdmin) {
		this.queryAdmin = queryAdmin;
	}

}
