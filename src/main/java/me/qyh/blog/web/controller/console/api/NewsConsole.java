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
package me.qyh.blog.web.controller.console.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.validator.NewsArchivePageQueryParamValidator;
import me.qyh.blog.core.validator.NewsValidator;
import me.qyh.blog.core.vo.NewsArchive;
import me.qyh.blog.core.vo.NewsArchivePageQueryParam;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class NewsConsole extends BaseMgrController {
	@Autowired
	private NewsValidator newsValidator;
	@Autowired
	private NewsService newsService;
	@Autowired
	private NewsArchivePageQueryParamValidator newsArchivePageQueryParamValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "news")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(newsValidator);
	}

	@InitBinder(value = "newsArchivePageQueryParam")
	protected void initQueryBinder(WebDataBinder binder) {
		binder.setValidator(newsArchivePageQueryParamValidator);
	}

	@GetMapping("newses")
	public PageResult<NewsArchive> findNewses(@Validated NewsArchivePageQueryParam newsArchivePageQueryParam) {
		newsArchivePageQueryParam.setQueryPrivate(true);
		newsArchivePageQueryParam.setPageSize(configServer.getGlobalConfig().getNewsPageSize());
		return newsService.queryNewsArchive(newsArchivePageQueryParam);
	}

	@GetMapping("news/{id}")
	public ResponseEntity<News> getNews(@PathVariable("id") Integer id) {
		Optional<News> op = newsService.getNews(id);
		return ResponseEntity.of(op);
	}

	@DeleteMapping("news/{id}")
	public ResponseEntity<Void> del(@PathVariable("id") Integer id) throws LogicException {
		newsService.deleteNews(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("news")
	public ResponseEntity<Void> write(@Validated @RequestBody News news) throws LogicException {
		newsService.saveNews(news);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("news/{id}")
	public ResponseEntity<Void> update(@Validated @RequestBody News news, @PathVariable("id") Integer id)
			throws LogicException {
		news.setId(id);
		newsService.updateNews(news);
		return ResponseEntity.noContent().build();
	}
}
