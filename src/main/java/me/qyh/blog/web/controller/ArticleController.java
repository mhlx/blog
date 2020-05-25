package me.qyh.blog.web.controller;

import java.util.List;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
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
import me.qyh.blog.vo.ArticleCategoryStatistic;
import me.qyh.blog.vo.ArticleQueryParam;
import me.qyh.blog.vo.ArticleStatistic;
import me.qyh.blog.vo.ArticleTagStatistic;
import me.qyh.blog.vo.PageResult;

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

	@GetMapping("articles/{idOrAlias}")
	public Article get(@PathVariable("idOrAlias") String idOrAlias) {
		return articleService.getArticle(idOrAlias)
				.orElseThrow(() -> new ResourceNotFoundException("article.notExists", "文章不存在"));
	}

	@GetMapping("article/archives")
	public PageResult<ArticleArchive> queryArticleArchives(@Valid ArticleArchiveQueryParam param) {
		return articleService.queryArticleArchives(param);
	}

	@GetMapping("article/statistic")
	public ArticleStatistic getArticleStatistic() {
		return articleService.getArticleStatistic();
	}

	@GetMapping("article/category/statistics")
	public List<ArticleCategoryStatistic> getArticleCategoryStatistics() {
		return articleService.getArticleCategoryStatistic();
	}

	@GetMapping("article/tag/statistics")
	public List<ArticleTagStatistic> getArticleTagStatistics() {
		return articleService.getArticleTagStatistic(null);
	}

	@GetMapping("article/category/{category}/tag/statistics")
	public List<ArticleTagStatistic> getArticleTagStatistics(@PathVariable("category") String category) {
		return articleService.getArticleTagStatistic(category);
	}

	@GetMapping("articles/{idOrAlias}/next")
	public Article nextArticle(@PathVariable("idOrAlias") String idOrAlias,
			@RequestParam(name = "categories", required = false) Set<String> categories,
			@RequestParam(name = "tags", required = false) Set<String> tags) {
		return articleService.next(idOrAlias, categories, tags)
				.orElseThrow(() -> new ResourceNotFoundException("article.next.notExists", "下一篇文章不存在"));
	}

	@GetMapping("articles/{idOrAlias}/previous")
	public Article prevArticle(@PathVariable("idOrAlias") String idOrAlias,
			@RequestParam(name = "categories", required = false) Set<String> categories,
			@RequestParam(name = "tags", required = false) Set<String> tags) {
		return articleService.prev(idOrAlias, categories, tags)
				.orElseThrow(() -> new ResourceNotFoundException("article.prev.notExists", "上一篇文章不存在"));
	}

	@GetMapping("articles")
	public PageResult<Article> query(@Valid ArticleQueryParam param) {
		param.setQueryPrivate(true);
		param.setQueryPasswordProtected(true);
		if (param.getStatus() == null) {
			param.setStatus(ArticleStatus.PUBLISHED);
		}
		param.setIgnorePaging(false);
		return articleService.queryArticle(param);
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
