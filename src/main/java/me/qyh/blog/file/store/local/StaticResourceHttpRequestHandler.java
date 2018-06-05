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
package me.qyh.blog.file.store.local;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用来管理系统的静态资源
 */
public class StaticResourceHttpRequestHandler extends EditablePathResourceHttpRequestHandler {

	@Autowired
	private ServletContext servletContext;
	
	private static final String PREFIX = "static";
	
	public StaticResourceHttpRequestHandler() {
		super();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setCacheSeconds(31556926);
		Path path = Paths.get(servletContext.getRealPath("/static"));
		init(path,PREFIX);
		super.afterPropertiesSet();
	}
	

}
