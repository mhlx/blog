package me.qyh.blog.web.controller.console;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.NewsService;

@Controller
@RequestMapping("console/news")
public class NewsMgrController extends BaseMgrController {

	@Autowired
	private NewsService newsService;

	@GetMapping
	public String index() {
		return "console/news/index";
	}

	@GetMapping("new")
	public String write(Model model) {
		model.addAttribute("news", new News());
		return "console/news/write";
	}

	@GetMapping("edit/{id}")
	public String update(@PathVariable("id") Integer id, RedirectAttributes ra, Model model) {
		Optional<News> optional = newsService.getNewsForEdit(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("news.notExists", "动态不存在"));
			return "redirect:/console/news";
		}
		News news = optional.get();
		model.addAttribute("news", news);
		model.addAttribute("editor", Editor.MD.name());
		return "console/news/write";
	}
}
