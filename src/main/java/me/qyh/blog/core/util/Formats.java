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
package me.qyh.blog.core.util;

/**
 * 用于页面处理
 * 
 * @author mhlx
 *
 */
public class Formats {

	private Formats() {
		super();
	}

	/**
	 * 将字节转化为可读的文件大小
	 * 
	 * @param bytes
	 * @param si
	 *            如果为true，那么以1000为一个单位，否则以1024为一个单位
	 * @return
	 */
	public static String readByte(long bytes, boolean si) {
		return FileUtils.humanReadableByteCount(bytes, si);
	}

	public static String readByte(long bytes) {
		return FileUtils.humanReadableByteCount(bytes, true);
	}

}
