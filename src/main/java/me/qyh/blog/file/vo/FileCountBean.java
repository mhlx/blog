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

public class FileCountBean {
	private int fileStore;
	private FileCount fileCount;

	public int getFileStore() {
		return fileStore;
	}

	public void setFileStore(int fileStore) {
		this.fileStore = fileStore;
	}

	public FileCount getFileCount() {
		return fileCount;
	}

	public void setFileCount(FileCount fileCount) {
		this.fileCount = fileCount;
	}

}