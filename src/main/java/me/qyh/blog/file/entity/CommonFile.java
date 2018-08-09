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
package me.qyh.blog.file.entity;

import java.util.Arrays;

import me.qyh.blog.core.entity.BaseEntity;
import me.qyh.blog.core.util.Validators;

/**
 * 系统存储的文件
 * 
 * @author Administrator
 *
 */
public class CommonFile extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long size;// 文件大小，该大小仅仅是本服务上的文件大小，并不代表其他存储服务上的文件大小
	private String extension;// 后缀名
	private int store;// 文件存储器
	private String originalFilename;// 原始文件名

	/**
	 * default
	 */
	public CommonFile() {
		super();
	}

	public CommonFile(CommonFile cf) {
		this.size = cf.size;
		this.extension = cf.extension;
		this.store = cf.store;
		this.id = cf.id;
		this.originalFilename = cf.originalFilename;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public int getStore() {
		return store;
	}

	public void setStore(int store) {
		this.store = store;
	}

	/**
	 * @since 5.10
	 * @param exts
	 * @return
	 */
	public boolean is(String... exts) {
		if (Validators.isEmpty(exts)) {
			return false;
		}
		if (extension == null) {
			return false;
		}
		return Arrays.stream(exts).anyMatch(ext -> ext.equalsIgnoreCase(extension));
	}
}
