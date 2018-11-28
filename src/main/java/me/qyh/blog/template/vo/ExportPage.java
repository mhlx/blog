package me.qyh.blog.template.vo;

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.entity.Page;

/**
 * 用于文件导入
 * 
 * @author Administrator
 *
 */
public class ExportPage {
	private Page page;
	private List<Fragment> fragments = new ArrayList<>();

	public ExportPage() {
		super();
	}

	public Page getPage() {
		return page;
	}

	public List<Fragment> getFragments() {
		return fragments;
	}

	public void add(Fragment fragment) {
		this.fragments.add(fragment);
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}
}
