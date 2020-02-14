package me.qyh.blog.web.backend.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.entity.ArticleValidator;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.service.ArticleService;
import me.qyh.blog.vo.ArticleQueryParam;

@Controller
@RequestMapping("console")
public class ArticleBackgroudController {

	@InitBinder("article")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(new ArticleValidator());
	}

	private final ArticleService articleService;

	public ArticleBackgroudController(ArticleService articleService) {
		super();
		this.articleService = articleService;
	}

	@ResponseBody
	@PostMapping("article/save")
	public int create(@Valid @RequestBody Article article) {
		return articleService.saveArticle(article);
	}

	@ResponseBody
	@PostMapping("articles/{idOrAlias}")
	public Article get(@PathVariable("idOrAlias") String idOrAlias) {
		return articleService.getArticle(idOrAlias).orElseThrow(() -> new LogicException("article.notExists", "文章不存在"));
	}

	@ResponseBody
	@PostMapping("articles/{id}/update")
	public void updateArticle(@PathVariable("id") int id, @Valid @RequestBody Article article) {
		article.setId(id);
		articleService.updateArticle(article);
	}

	@GetMapping("article/write")
	public String write() {
		return "console/article/write";
	}

	@GetMapping("articles/{id}/edit")
	public String edit(@PathVariable("id") int id, Model model) {
		model.addAttribute("article", articleService.getArticleForEdit(id)
				.orElseThrow(() -> new ResourceNotFoundException("article.notExists", "文章不存在")));
		return "console/article/edit";
	}

	@PostMapping("articles/{id}/delete")
	@ResponseBody
	public void delete(@PathVariable("id") int id) {
		articleService.deleteArticle(id);
	}

	@GetMapping("articles")
	public String index(@Valid ArticleQueryParam param, Model model) {
		param.setQueryPrivate(true);
		param.setQueryPasswordProtected(true);
		if (param.getStatus() == null) {
			param.setStatus(ArticleStatus.PUBLISHED);
		}
		if (!param.hasPageSize()) {
			param.setPageSize(10);
		}
		param.setIgnorePaging(false);
		model.addAttribute("page", articleService.queryArticle(param));
		model.addAttribute("statistic", articleService.getArticleStatistic());
		return "console/article/index";
	}

}
