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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.context.Environment;

/**
 * 私人文件存储器
 * <p><b>不应该被nginx等服务器代理！！！</b></p>
 */
public class PrivateResourceStore extends CommonResourceStore{

	public PrivateResourceStore(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}
	
	public PrivateResourceStore() {
		super("private");
	}

	@Override
	protected final Resource findResource(HttpServletRequest request) throws IOException {
		Environment.doAuthencation();
		return super.findResource(request);
	}

	@Override
	protected final boolean getRegisterMapping() {
		return true;
	}	


}
