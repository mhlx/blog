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
package me.qyh.blog.file.store.local;

import org.springframework.web.multipart.MultipartFile;

/**
 * 非图片文件存储器
 * 
 * @author Administrator
 *
 */
public class CommonResourceStore extends LocalResourceRequestHandlerFileStore {

	public CommonResourceStore(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}

	public CommonResourceStore() {
		super("file");
	}

	@Override
	public final boolean canStore(MultipartFile multipartFile) {
		return true;
	}
}
