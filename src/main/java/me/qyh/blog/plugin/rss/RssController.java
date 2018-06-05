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
package me.qyh.blog.plugin.rss;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.PageResult;

@Controller
public class RssController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private RssView rssView;
	@Autowired
	private ConfigServer configServer;

	@GetMapping({ "rss", "space/{alias}/rss" })
	public View rss(ModelMap model) {

		ArticleQueryParam param = new ArticleQueryParam();
		param.setCurrentPage(1);
		Space space = Environment.getSpace();
		param.setStatus(ArticleStatus.PUBLISHED);
		param.setSpace(space);
		param.setIgnoreLevel(true);
		param.setQueryLock(false);
		param.setQueryPrivate(false);
		param.setSort(null);
		param.setIgnorePaging(false);
		param.setPageSize(configServer.getGlobalConfig().getArticlePageSize());
		PageResult<Article> page = articleService.queryArticle(param);
		model.addAttribute("page", page);
		return rssView;
	}

}
