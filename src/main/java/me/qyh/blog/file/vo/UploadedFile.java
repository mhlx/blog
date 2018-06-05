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

import me.qyh.blog.core.message.Message;
import me.qyh.blog.file.store.ThumbnailUrl;

/**
 * 文件上传结果
 * 
 * @author mhlx
 *
 */
public class UploadedFile {
	private Message error;
	private long size;// 上传文件大小
	private String name;// 上传文件名称
	private ThumbnailUrl thumbnailUrl;// 缩略图路径
	private String url;// 访问路径

	/**
	 * 
	 * @param name
	 *            失败文件名
	 * @param error
	 *            失败信息
	 */
	public UploadedFile(String name, Message error) {
		this.error = error;
		this.name = name;
	}

	/**
	 * 
	 * @param name
	 *            文件名
	 * @param size
	 *            文件大小
	 * @param thumbnailUrl
	 *            缩略图链接
	 * @param url
	 *            访问链接
	 */
	public UploadedFile(String name, long size, ThumbnailUrl thumbnailUrl, String url) {
		this.url = url;
		this.size = size;
		this.name = name;
		this.thumbnailUrl = thumbnailUrl;
	}

	public Message getError() {
		return error;
	}

	public long getSize() {
		return size;
	}

	public String getName() {
		return name;
	}

	/**
	 * 是否存在异常
	 * 
	 * @return
	 */
	public boolean hasError() {
		return error != null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ThumbnailUrl getThumbnailUrl() {
		return thumbnailUrl;
	}

}
