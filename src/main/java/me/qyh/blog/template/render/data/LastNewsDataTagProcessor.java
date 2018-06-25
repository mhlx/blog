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
package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;

public class LastNewsDataTagProcessor extends DataTagProcessor<List<News>> {

	@Autowired
	private NewsService newsService;

	private static final int DEFAULT_LIMIT = 5;
	private static final int MAX_LIMIT = 20;

	public LastNewsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<News> query(Attributes attributes) throws LogicException {
		return newsService.queryLastNews(getLimit(attributes));
	}

	private int getLimit(Attributes attributes) {
		return attributes.getInteger("limit").filter(limit -> limit > 0 && limit <= MAX_LIMIT).orElse(DEFAULT_LIMIT);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("limit");
	}
}
