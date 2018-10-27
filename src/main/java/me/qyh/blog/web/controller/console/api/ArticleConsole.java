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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.ArticleQueryParamValidator;
import me.qyh.blog.core.validator.ArticleValidator;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.web.controller.console.BaseMgrController;

/**
 * 文章管理控制器
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("api/console")
public class ArticleConsole extends BaseMgrController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private ArticleValidator articleValidator;
	@Autowired
	private ArticleQueryParamValidator articleQueryParamValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "article")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(articleValidator);
	}

	@InitBinder(value = "articleQueryParam")
	protected void initQueryBinder(WebDataBinder binder) {
		binder.setValidator(articleQueryParamValidator);
	}

	@GetMapping("articles")
	public PageResult<Article> findArticles(@Validated ArticleQueryParam articleQueryParam) {
		if (articleQueryParam.getStatus() == null) {
			articleQueryParam.setStatus(ArticleStatus.PUBLISHED);
		}
		articleQueryParam.setQueryPrivate(true);
		articleQueryParam.setIgnorePaging(false);
		articleQueryParam.setPageSize(configServer.getGlobalConfig().getArticlePageSize());
		return articleService.queryArticle(articleQueryParam);
	}

	@GetMapping("article/{id}")
	public ResponseEntity<Article> getArticle(@PathVariable("id") Integer id) {
		Optional<Article> op = articleService.getArticleForEdit(id);
		return ResponseEntity.of(op);
	}

	@PutMapping("article/{id}")
	public ResponseEntity<Void> update(@RequestBody @Validated Article article, @PathVariable("id") Integer id)
			throws LogicException {
		if (Validators.isEmptyOrNull(article.getFeatureImage(), true)) {
			article.setFeatureImage(null);
		}
		article.setId(id);
		articleService.updateArticle(article);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("article")
	public ResponseEntity<Article> write(@RequestBody @Validated Article article) throws LogicException {
		Article saved = articleService.writeArticle(article);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PostMapping("article/preview")
	public ResponseEntity<String> createPreviewContent(@RequestParam("editor") Editor editor,
			@RequestParam("content") String content) throws LogicException {
		String previewContent = articleService.createPreviewContent(editor, content);
		return ResponseEntity.status(HttpStatus.CREATED).body(previewContent);
	}

	@DeleteMapping("article/{id}")
	public ResponseEntity<Void> deleteArticle(@PathVariable("id") Integer id) throws LogicException {
		articleService.deleteArticle(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(name = "article/{id}")
	public ResponseEntity<Void> update(@PathVariable("id") Integer id, ArticleStatus status) throws LogicException {
		switch (status) {
		case PUBLISHED:
			articleService.recoverArticle(id);
			break;
		case DELETED:
			articleService.putArticleInRecycleBin(id);
			break;
		default:
			// TODO
			throw new LogicException("");
		}
		return ResponseEntity.noContent().build();
	}

}
