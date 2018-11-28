package me.qyh.blog.web.controller.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.CommentServer;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.service.TagService;
import me.qyh.blog.core.vo.CommentStatistics;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.file.service.FileService;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("console/statistics")
public class StatisticsController extends BaseMgrController {

	@Autowired
	private FileService fileService;
	@Autowired
	private ArticleService articleService;
	@Autowired(required = false)
	private CommentServer commentServer;
	@Autowired
	private TagService tagService;
	@Autowired
	private TemplateService templateService;
	@Autowired
	private SpaceService spaceService;
	@Autowired
	private NewsService newsService;

	@GetMapping
	public String queryStatisticsDetail(@RequestParam(value = "spaceId", required = false) Integer spaceId, Model model,
			RedirectAttributes ra) {
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		try {
			Space space = spaceId == null ? null
					: spaceService.getSpace(spaceId).orElseThrow(() -> new LogicException("space.notExists", "空间不存在"));
			StatisticsDetail detail = new StatisticsDetail();
			detail.setArticleStatistics(articleService.queryArticleDetailStatistics(space));
			detail.setCommentStatistics(
					commentServer == null ? new CommentStatistics() : commentServer.queryCommentStatistics(space));

			if (space == null) {
				detail.setFileStatistics(fileService.queryFileStatistics());
				detail.setNewsStatistics(newsService.queryNewsStatistics());
			}

			detail.setPageStatistics(templateService.queryPageStatistics(space));
			detail.setTagStatistics(tagService.queryTagDetailStatistics(space));

			model.addAttribute("statistics", detail);
			return "console/statistics/index";
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/console/statistics";
		}
	}

}
