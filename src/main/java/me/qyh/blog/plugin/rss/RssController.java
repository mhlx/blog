package me.qyh.blog.plugin.rss;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public View rss(Model model) {

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
