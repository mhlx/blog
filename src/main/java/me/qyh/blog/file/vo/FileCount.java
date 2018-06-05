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
public class FileCount {
		private int fileCount;// 文件数量
		private long totalSize;// 文件总大小

		public int getFileCount() {
			return fileCount;
		}

		public void setFileCount(int fileCount) {
			this.fileCount = fileCount;
		}

		public long getTotalSize() {
			return totalSize;
		}

		public void setTotalSize(long totalSize) {
			this.totalSize = totalSize;
		}
	}

	