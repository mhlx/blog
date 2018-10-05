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
package me.qyh.blog.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.plugin.ArticleContentHandlerRegistry;
import me.qyh.blog.core.service.ArticleContentHandler;

/**
 * 用于支持多个ArticleContentHandler
 * 
 * @author Administrator
 *
 */
@Component
public class ArticleContentHandlers implements ArticleContentHandler, ArticleContentHandlerRegistry {

	private final List<ArticleContentHandler> handlers = new ArrayList<>();

	@Override
	public String handle(String content) {
		String handled = content;
		if (!CollectionUtils.isEmpty(handlers)) {
			for (ArticleContentHandler handler : handlers) {
				handled = Objects.requireNonNull(handler.handle(handled));
			}
		}
		return handled;
	}

	@Override
	public ArticleContentHandlerRegistry register(ArticleContentHandler handler) {
		this.handlers.add(handler);
		return this;
	}

}
