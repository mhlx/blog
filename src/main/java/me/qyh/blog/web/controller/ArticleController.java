package me.qyh.blog.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.entity.ArticleValidator;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.utils.WebUtils;
import me.qyh.blog.vo.ArticleArchive;
import me.qyh.blog.vo.ArticleArchiveQueryParam;
import me.qyh.blog.vo.ArticleQueryParam;
import me.qyh.blog.vo.ArticleStatistic;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.TemplateDataMapping;

@RestController
@RequestMapping("api")
@Authenticated
public class ArticleController {

	@InitBinder("article")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(new ArticleValidator());
	}

	private final ArticleService articleService;
	private final BlogProperties blogProperties;

	public ArticleController(ArticleService articleService, BlogProperties blogProperties) {
		super();
		this.articleService = articleService;
		this.blogProperties = blogProperties;
	}

	@TemplateDataMapping("categories/{category}/articles/{idOrAlias}")
	public Article getArticle(@PathVariable("category") String category, @PathVariable("idOrAlias") String idOrAlias) {
		return articleService.getArticle(idOrAlias, category)
				.orElseThrow(() -> new ResourceNotFoundException("article.notExists", "文章不存在"));
	}

	@TemplateDataMapping("articles/{idOrAlias}")
	public Article get(@PathVariable("idOrAlias") String idOrAlias) {
		return getArticle(null, idOrAlias);
	}

	@TemplateDataMapping("categories/{category}/articleArchives")
	public PageResult<ArticleArchive> queryArticleArchives(@PathVariable("category") String category,
			@Valid ArticleArchiveQueryParam param) {
		param.setCategory(category);
		return articleService.queryArticleArchives(param);
	}

	@TemplateDataMapping("articleArchives")
	public PageResult<ArticleArchive> queryArticleArchives(@Valid ArticleArchiveQueryParam param) {
		return queryArticleArchives(null, param);
	}

	@TemplateDataMapping("articleStatistic")
	public ArticleStatistic getArticleStatistic() {
		return articleService.getArticleStatistic(null);
	}

	@TemplateDataMapping("categories/{category}/articleStatistic")
	public ArticleStatistic getArticleStatistic(@PathVariable("category") String category) {
		return articleService.getArticleStatistic(category);
	}

	@TemplateDataMapping("categories/{category}/articles/{idOrAlias}/next")
	public Article nextArticle(@PathVariable("category") String category, @PathVariable("idOrAlias") String idOrAlias) {
		return articleService.next(idOrAlias, category)
				.orElseThrow(() -> new ResourceNotFoundException("article.next.notExists", "下一篇文章不存在"));
	}

	@TemplateDataMapping("articles/{idOrAlias}/next")
	public Article nextArticle(@PathVariable("idOrAlias") String idOrAlias) {
		return nextArticle(null, idOrAlias);
	}

	@TemplateDataMapping("categories/{category}/articles/{idOrAlias}/previous")
	public Article prevArticle(@PathVariable("category") String category, @PathVariable("idOrAlias") String idOrAlias) {
		return articleService.prev(idOrAlias, category)
				.orElseThrow(() -> new ResourceNotFoundException("article.prev.notExists", "上一篇文章不存在"));
	}

	@TemplateDataMapping("articles/{idOrAlias}/previous")
	public Article prevArticle(@PathVariable("idOrAlias") String idOrAlias) {
		return prevArticle(null, idOrAlias);
	}

	@TemplateDataMapping("categories/{category}/articles")
	public PageResult<Article> query(@PathVariable("category") String category, @Valid ArticleQueryParam param) {
		param.setQueryPrivate(true);
		param.setQueryPasswordProtected(true);
		if (param.getStatus() == null) {
			param.setStatus(ArticleStatus.PUBLISHED);
		}
		param.setIgnorePaging(false);
		param.setCategory(category);
		return articleService.queryArticle(param);
	}

	@TemplateDataMapping("articles")
	public PageResult<Article> query(@Valid ArticleQueryParam param) {
		return query(null, param);
	}

	@Authenticated(required = false)
	@PatchMapping("articles/{id}/hit")
	public ResponseEntity<?> hitArticle(@PathVariable("id") int id, HttpServletRequest request) {
		if (WebUtils.isSpider(request)) {
			return ResponseEntity.noContent().build();
		}
		articleService.hit(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("article")
	public ResponseEntity<Integer> create(@Valid @RequestBody Article article) {
		int id = articleService.saveArticle(article);
		return ResponseEntity.created(blogProperties.buildUrl("api/editableArticles/" + id)).body(id);
	}

	@PutMapping("articles/{id}")
	public ResponseEntity<?> updateArticle(@PathVariable("id") int id, @Valid @RequestBody Article article) {
		article.setId(id);
		articleService.updateArticle(article);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("articles/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") int id) {
		articleService.deleteArticle(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("editableArticles/{id}")
	public Article getArticle(@PathVariable("id") int id) {
		return articleService.getArticleForEdit(id)
				.orElseThrow(() -> new ResourceNotFoundException("article.notExists", "文章不存在"));
	}
}
