/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
