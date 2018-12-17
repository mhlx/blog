package me.qyh.blog.web.controller.console;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.validator.ArticleValidator;
import me.qyh.blog.core.vo.SpaceQueryParam;

/**
 * 文章管理控制器
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("console/article")
public class ArticleMgrController extends BaseMgrController {

	@Autowired
	private SpaceService spaceService;
	@Autowired
	private ArticleService articleService;
	@Autowired
	private ArticleValidator articleValidator;

	@InitBinder(value = "article")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(articleValidator);
	}

	@GetMapping
	public String index() {
		return "console/article/index";
	}

	@GetMapping("new")
	public String write(RedirectAttributes ra, Model model) {
		SpaceQueryParam param = new SpaceQueryParam();
		List<Space> spaces = spaceService.querySpace(param);
		if (spaces.isEmpty()) {
			// 没有任何可用空间，跳转到空间管理页面
			ra.addFlashAttribute(Constants.ERROR, new Message("artic.write.noSpace", "在撰写文章之前，应该首先创建一个可用的空间"));
			return "redirect:/console/space";
		}
		model.addAttribute("spaces", spaces);
		model.addAttribute("editor", Editor.MD.name());
		model.addAttribute("article", new Article());
		/**
		 * @since 7.0
		 */
		// if (Editor.MD.equals(editor)) {
		return "console/article/write/new_md";
		// }
		// return "mgr/article/write/editor";
	}

	@GetMapping("edit/{id}")
	public String update(@PathVariable("id") Integer id, RedirectAttributes ra, Model model) {
		Optional<Article> optional = articleService.getArticleForEdit(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("article.notExists", "文章不存在"));
			return "redirect:/console/article";
		}
		Article article = optional.get();
		model.addAttribute("article", article);
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		model.addAttribute("editor", Editor.MD.name());
		/**
		 * @since 7.0
		 */
		// if (Editor.MD.equals(article.getEditor())) {
		return "console/article/write/new_md";
		// }
		// return "mgr/article/write/editor";
	}

}
