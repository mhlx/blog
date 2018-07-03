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
package me.qyh.blog.core.service;

import me.qyh.blog.core.service.impl.ArticleIndexer;
import me.qyh.blog.core.service.impl.ArticleServiceImpl;

/**
 * 文章内容处理器，用于文章内容的调整,<b>同时也将用于构建索引时文章内容的预处理</b>
 * <p>
 * 可以为空
 * </p>
 * 
 * @see ArticleIndexer
 * @see ArticleServiceImpl
 * 
 * @author Administrator
 *
 */
public interface ArticleContentHandler {
	/**
	 * 用来处理文章
	 * 
	 * @return 处理后的内容
	 * @param content
	 *            文章内容 <b>HTML文本</b>
	 */
	String handle(String content);

	/**
	 * 用来处理预览文章
	 * 
	 * @return 处理后的内容
	 * @param content
	 *            文章内容 <b>HTML文本</b>
	 */
	default String handlePreview(String content) {
		return handle(content);
	}
}