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

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.core.vo.PageResult;

public class NewsPageDataTagProcessor extends DataTagProcessor<PageResult<News>> {

	@Autowired
	private NewsService newsService;
	@Autowired
	private ConfigServer configServer;

	public NewsPageDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<News> query(Attributes attributes) throws LogicException {
		NewsQueryParam param = new NewsQueryParam();
		String beginStr = attributes.getString("begin").orElse(null);
		String endStr = attributes.getString("end").orElse(null);
		if (beginStr != null && endStr != null) {
			param.setBegin(Times.parseAndGetDate(beginStr));
			param.setEnd(Times.parseAndGetDate(endStr));
		}
		if (Environment.isLogin()) {
			param.setQueryPrivate(attributes.getBoolean("queryPrivate").orElse(true));
		}
		attributes.getString("content").ifPresent(param::setContent);
		param.setAsc(attributes.getBoolean("asc").orElse(false));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(1));

		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}

		int pageSize = configServer.getGlobalConfig().getNewsPageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		return newsService.queryNews(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("begin", "end", "queryPrivate", "asc", "pageSize", "currentPage");
	}
}
