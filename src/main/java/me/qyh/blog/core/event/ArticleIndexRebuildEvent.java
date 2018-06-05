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

import org.springframework.context.ApplicationEvent;

/**
 * 文章索引重建事件
 * <p>
 * 这个事件应该在事务提交|回滚后推送，例如
 * 
 * <pre>
 * Transactions.afterCommit(() -> applicationEventPublisher.publishEvent(new ArticleIndexRebuildEvent(this)));
 * </pre>
 * </p>
 * 
 * @author Administrator
 *
 */
public class ArticleIndexRebuildEvent extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArticleIndexRebuildEvent(Object source) {
		super(source);
	}

}
