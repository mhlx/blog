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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.ArticleArchive;
import me.qyh.blog.core.vo.ArticleArchivePageQueryParam;
import me.qyh.blog.core.vo.PageResult;

/**
 * 文章归档
 * 
 * @author Administrator
 *
 */
public class ArticleArchivesDataTagProcessor extends DataTagProcessor<PageResult<ArticleArchive>> {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private ConfigServer configServer;

	public ArticleArchivesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<ArticleArchive> query(Attributes attributes) throws LogicException {
		ArticleArchivePageQueryParam param = new ArticleArchivePageQueryParam();
		String ymd = attributes.getString("ymd").orElse(null);
		if (ymd != null) {
			try {
				LocalDate.parse(ymd);
			} catch (DateTimeParseException e) {
				ymd = null;
			}
		}
		param.setYmd(ymd);
		param.setQueryPrivate(attributes.getBoolean("queryPrivate").orElse(true));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		int pageSize = configServer.getGlobalConfig().getArticleArchivePageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(1));
		return articleService.selectArticleArchives(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("currentPage", "pageSize", "queryPrivate", "ymd");
	}

}
