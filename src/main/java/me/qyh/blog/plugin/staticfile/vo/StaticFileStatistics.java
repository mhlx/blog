/*
 * Copyright 2017 qyh.me
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

public class StaticFileStatistics {

	private int dirCount;// 文件夹数目
	private int fileCount;// 文件数目
	private long fileSize;// 文件总大小

	public StaticFileStatistics() {
		super();
	}

	public StaticFileStatistics(int dirCount, int fileCount, long fileSize) {
		super();
		this.dirCount = dirCount;
		this.fileCount = fileCount;
		this.fileSize = fileSize;
	}

	public int getDirCount() {
		return dirCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public void setDirCount(int dirCount) {
		this.dirCount = dirCount;
	}

}
