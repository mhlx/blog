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
package me.qyh.blog.core.config;

/**
 * 全局配置
 * 
 * @author mhlx
 *
 */
public class GlobalConfig {

	/**
	 * 文件管理每页数量
	 */
	private int filePageSize;

	/**
	 * 用户模板片段管理分页数量
	 */
	private int fragmentPageSize;

	/**
	 * 用户自定义页面分页数量
	 */
	private int pagePageSize;

	/**
	 * 文章页面分页数量
	 */
	private int articlePageSize;

	/**
	 * 标签页面分页数量
	 */
	private int tagPageSize;

	private int newsPageSize;

	public int getFilePageSize() {
		return filePageSize;
	}

	public void setFilePageSize(int filePageSize) {
		this.filePageSize = filePageSize;
	}

	public int getFragmentPageSize() {
		return fragmentPageSize;
	}

	public void setFragmentPageSize(int fragmentPageSize) {
		this.fragmentPageSize = fragmentPageSize;
	}

	public int getPagePageSize() {
		return pagePageSize;
	}

	public void setPagePageSize(int pagePageSize) {
		this.pagePageSize = pagePageSize;
	}

	public int getArticlePageSize() {
		return articlePageSize;
	}

	public void setArticlePageSize(int articlePageSize) {
		this.articlePageSize = articlePageSize;
	}

	public int getTagPageSize() {
		return tagPageSize;
	}

	public void setTagPageSize(int tagPageSize) {
		this.tagPageSize = tagPageSize;
	}

	public int getNewsPageSize() {
		return newsPageSize;
	}

	public void setNewsPageSize(int newsPageSize) {
		this.newsPageSize = newsPageSize;
	}
}
