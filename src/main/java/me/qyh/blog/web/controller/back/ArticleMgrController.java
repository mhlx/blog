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
package me.qyh.blog.web.controller.back;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.ArticleQueryParamValidator;
import me.qyh.blog.core.validator.ArticleValidator;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.SpaceQueryParam;

/**
 * 、 文章管理控制器
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("mgr/article")
public class ArticleMgrController extends BaseMgrController {

	@Autowired
	private SpaceService spaceService;
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

	@GetMapping("index")
	public String index(@Validated ArticleQueryParam articleQueryParam, Model model) {
		if (articleQueryParam.getStatus() == null) {
			articleQueryParam.setStatus(ArticleStatus.PUBLISHED);
		}
		articleQueryParam.setQueryPrivate(true);
		articleQueryParam.setIgnorePaging(false);
		articleQueryParam.setPageSize(configServer.getGlobalConfig().getArticlePageSize());
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		model.addAttribute("page", articleService.queryArticle(articleQueryParam));
		return "mgr/article/index";
	}

	@PostMapping("logicDelete")
	@ResponseBody
	public JsonResult logicDelete(@RequestParam("id") Integer id) throws LogicException {
		articleService.logicDeleteArticle(id);
		return new JsonResult(true, new Message("article.logicDelete.success", "放入回收站成功"));
	}

	@PostMapping("recover")
	@ResponseBody
	public JsonResult recover(@RequestParam("id") Integer id) throws LogicException {
		articleService.recoverArticle(id);
		return new JsonResult(true, new Message("article.recover.success", "恢复成功"));
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam("id") Integer id) throws LogicException {
		articleService.deleteArticle(id);
		return new JsonResult(true, new Message("article.delete.success", "删除成功"));
	}

	@GetMapping("write")
	public String write(@RequestParam(value = "editor", required = false, defaultValue = "MD") Editor editor,
			RedirectAttributes ra, Model model) {
		SpaceQueryParam param = new SpaceQueryParam();
		List<Space> spaces = spaceService.querySpace(param);
		if (spaces.isEmpty()) {
			// 没有任何可用空间，跳转到空间管理页面
			ra.addFlashAttribute(Constants.ERROR, new Message("artic.write.noSpace", "在撰写文章之前，应该首先创建一个可用的空间"));
			return "redirect:/mgr/space/index";
		}
		model.addAttribute("spaces", spaces);
		model.addAttribute("editor", editor.name());
		model.addAttribute("article", new Article());
		/**
		 * @since 2017/12/2
		 */
		if (Editor.MD.equals(editor)) {
			return "mgr/article/write/new_md";
		}
		return "mgr/article/write/editor";
	}

	@GetMapping("write/preview")
	public String preview() {
		return "mgr/article/write/preview";
	}

	/**
	 * @since 2017/12/2
	 */
	@GetMapping("write/mdPreview")
	public String mdPreview() {
		return "mgr/article/write/new_preview";
	}

	@PostMapping("write")
	@ResponseBody
	public JsonResult write(@RequestBody @Validated Article article) throws LogicException {
		if (Validators.isEmptyOrNull(article.getFeatureImage(), true)) {
			article.setFeatureImage(null);
		}
		return new JsonResult(true, articleService.writeArticle(article));
	}

	@PostMapping("update")
	@ResponseBody
	public JsonResult update(@RequestBody @Validated Article article) throws LogicException {
		if (Validators.isEmptyOrNull(article.getFeatureImage(), true)) {
			article.setFeatureImage(null);
		}
		return new JsonResult(true, articleService.updateArticle(article));
	}

	@PostMapping("pub")
	@ResponseBody
	public JsonResult pub(@RequestParam("id") Integer id) throws LogicException {
		articleService.publishDraft(id);
		return new JsonResult(true, "article.pub.success");
	}

	@GetMapping("update/{id}")
	public String update(@PathVariable("id") Integer id, RedirectAttributes ra, Model model) {
		Optional<Article> optional = articleService.getArticleForEdit(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("article.notExists", "文章不存在"));
			return "redirect:/mgr/article/index";
		}
		Article article = optional.get();
		model.addAttribute("article", article);
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		model.addAttribute("editor", article.getEditor().name());
		/**
		 * @since 2017/12/2
		 */
		if (Editor.MD.equals(article.getEditor())) {
			return "mgr/article/write/new_md";
		}
		return "mgr/article/write/editor";
	}

	@PostMapping("write/preview")
	@ResponseBody
	public JsonResult preview(@RequestBody Article article) {
		if (article.getEditor() == null || Validators.isEmptyOrNull(article.getContent(), false)) {
			return new JsonResult(true, "");
		}
		articleService.preparePreview(article);
		return new JsonResult(true, article.getContent());
	}

}
