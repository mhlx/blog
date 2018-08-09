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
	
	public boolean needQuery(){
		return !CollectionUtils.isEmpty(extensions) || !Validators.isEmptyOrNull(name, true); 
	}

}
