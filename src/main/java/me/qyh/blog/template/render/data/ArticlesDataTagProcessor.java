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

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.MapBindingResult;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleFrom;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.ArticleQueryParamValidator;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.ArticleQueryParam.Sort;
import me.qyh.blog.core.vo.PageResult;

/**
 * 文章列表数据处理器
 * 
 * @author Administrator
 *
 */
public class ArticlesDataTagProcessor extends DataTagProcessor<PageResult<Article>> {

	@Autowired
	private ArticleQueryParamValidator validator;
	@Autowired
	private ArticleService articleService;
	@Autowired
	private ConfigServer configServer;

	/**
	 * 构造器
	 * 
	 * @param name
	 *            数据处理器名称
	 * @param dataName
	 *            页面dataName
	 */
	public ArticlesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<Article> query(Attributes attributes) throws LogicException {
		ArticleQueryParam param = buildFromAttributes(attributes);
		return articleService.queryArticle(param);
	}

	private ArticleQueryParam buildFromAttributes(Attributes attributes) {
		ArticleQueryParam param = new ArticleQueryParam();

		String beginStr = attributes.getString("begin").orElse(null);
		String endStr = attributes.getString("end").orElse(null);
		if (beginStr != null && endStr != null) {
			param.setBegin(Times.parseAndGetDate(beginStr));
			param.setEnd(Times.parseAndGetDate(endStr));
		}

		String query = attributes.getString("query").orElse(null);
		if (!Validators.isEmptyOrNull(query, true)) {
			param.setQuery(query);
		}

		attributes.getEnum("from", ArticleFrom.class).ifPresent(param::setFrom);
		attributes.getString("tag").ifPresent(param::setTag);
		attributes.getEnum("sort", Sort.class).ifPresent(param::setSort);
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(0));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		param.setHighlight(attributes.getBoolean("highlight").orElse(true));

		attributes.getBoolean("ignoreLevel").ifPresent(param::setIgnoreLevel);

		param.setQueryLock(attributes.getBoolean("queryLock").orElse(true));
		param.setSpaces(attributes.getSet("spaces", ","));

		attributes.getBoolean("ignorePaging").ifPresent(param::setIgnorePaging);

		param.setQueryPrivate(attributes.getBoolean("queryPrivate").orElse(true));

		param.setSpace(getCurrentSpace());
		param.setStatus(ArticleStatus.PUBLISHED);

		int pageSize = configServer.getGlobalConfig().getArticlePageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		validator.validate(param, new MapBindingResult(new HashMap<>(), "articleQueryParam"));
		return param;
	}

	@Override
	public List<String> getAttributes() {
		return List.of("begin", "end", "query", "from", "tag", "sort", "currentPage", "pageSize", "highlight",
				"ignoreLevel", "queryLock", "spaces", "ignorePaging", "queryPrivate");
	}
}
