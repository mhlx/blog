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
import me.qyh.blog.core.vo.NewsNav;

public class NewsNavDataTagProcessor extends DataTagProcessor<NewsNav> {

	private static final String ID = "id";
	private static final String REF_NEWS = "news";

	@Autowired
	private NewsService newsService;

	public NewsNavDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected NewsNav query(Attributes attributes) throws LogicException {
		Object v = attributes.get(REF_NEWS).orElse(null);
		if (v != null) {
			return newsService.getNewsNav((News) v).orElse(null);
		}
		return attributes.getInteger(ID).flatMap(id -> newsService.getNewsNav(id)).orElse(null);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("id", "ref-news");
	}
}
