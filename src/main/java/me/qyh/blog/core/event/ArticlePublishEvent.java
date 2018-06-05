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
 * 文章发布事件
 * <p>
 * 用户从草稿箱发布文章或者发布计划文章时将会触发该事件
 * </p>
 * 
 * @see ArticleCreateEvent
 * @see ArticleUpdateEvent
 * 
 * @author wwwqyhme
 *
 */
public class ArticlePublishEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Article> articles;

	public ArticlePublishEvent(Object source, List<Article> articles) {
		super(source);
		this.articles = articles;
	}

	public List<Article> getArticles() {
		return articles;
	}

}
