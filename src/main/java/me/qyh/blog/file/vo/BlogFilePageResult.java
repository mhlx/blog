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
