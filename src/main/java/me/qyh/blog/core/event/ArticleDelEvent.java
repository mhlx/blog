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
package me.qyh.blog.core.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.entity.Article;

/**
 * 用户逻辑删除|实际删除文章时触发
 * 
 * @author wwwqyhme
 *
 */
public class ArticleDelEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Article> articles;
	private final boolean logicDelete;

	public ArticleDelEvent(Object source, List<Article> articles, boolean logicDelete) {
		super(source);
		this.articles = articles;
		this.logicDelete = logicDelete;
	}

	public List<Article> getArticles() {
		return articles;
	}

	public boolean isLogicDelete() {
		return logicDelete;
	}

}
