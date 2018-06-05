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
package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.Article;

/**
 * 上一篇文章，下一篇文章
 * 
 * @author Administrator
 *
 */
public class ArticleNav {

	private Article previous;
	private Article next;

	/**
	 * 构造器
	 * 
	 * @param previous
	 *            上一篇文章
	 * @param next
	 *            下一篇文章
	 */
	public ArticleNav(Article previous, Article next) {
		this.previous = previous;
		this.next = next;
	}

	public Article getPrevious() {
		return previous;
	}

	public void setPrevious(Article previous) {
		this.previous = previous;
	}

	public Article getNext() {
		return next;
	}

	public void setNext(Article next) {
		this.next = next;
	}

}
