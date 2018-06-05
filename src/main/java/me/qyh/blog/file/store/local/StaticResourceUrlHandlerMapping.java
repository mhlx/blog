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

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import me.qyh.blog.core.plugin.ResourceHttpRequestHandlerMappingRegistry;

/**
 * UrlMapping，用來注册静态文件处理器
 */
public class StaticResourceUrlHandlerMapping extends SimpleUrlHandlerMapping
		implements ResourceHttpRequestHandlerMappingRegistry {

	public void registerResourceHttpRequestHandlerMapping(String urlPath, ResourceHttpRequestHandler handler)
			throws BeansException, IllegalStateException {
		super.registerHandler(urlPath, handler);
	}

	@Override
	public ResourceHttpRequestHandlerMappingRegistry registry(String urlPath, ResourceHttpRequestHandler handler) {
		registerHandler(urlPath, handler);
		return this;
	}

}