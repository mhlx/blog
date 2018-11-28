package me.qyh.blog.template.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.entity.Page;

public class PreviewImport {

	private final List<Page> pages = new ArrayList<>();
	private final List<Fragment> fragments = new ArrayList<>();

	public void addPages(Page... pages) {
		Collections.addAll(this.pages, pages);
	}

	public void addFragments(Fragment... fragments) {
		Collections.addAll(this.fragments, fragments);
	}

	public List<Page> getPages() {
		return pages;
	}

	public List<Fragment> getFragments() {
		return fragments;
	}

}
