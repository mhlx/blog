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
