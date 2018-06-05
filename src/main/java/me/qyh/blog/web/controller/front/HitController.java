package me.qyh.blog.web.controller.front;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.vo.JsonResult;

@Controller
public class HitController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private NewsService newsService;

	@PostMapping("space/{alias}/article/hit/{id}")
	@ResponseBody
	public JsonResult hitArticle(@PathVariable("id") Integer id, @RequestHeader("referer") String referer) {
		try {
			UriComponents uc = UriComponentsBuilder.fromHttpUrl(referer).build();
			if (!UrlUtils.match("/space/" + Environment.getSpaceAlias() + "/article/*", uc.getPath())
					&& !UrlUtils.match("/article/*", uc.getPath())) {
				return new JsonResult(false);
			}
		} catch (Exception e) {
			return new JsonResult(false);
		}

		articleService.hit(id);
		return new JsonResult(true);
	}

	@PostMapping("news/hit/{id}")
	@ResponseBody
	public JsonResult hitNews(@PathVariable("id") Integer id, @RequestHeader("referer") String referer) {
		try {
			UriComponents uc = UriComponentsBuilder.fromHttpUrl(referer).build();
			if (!UrlUtils.match("/news/*", uc.getPath())) {
				return new JsonResult(false);
			}
		} catch (Exception e) {
			return new JsonResult(false);
		}

		newsService.hit(id);
		return new JsonResult(true);
	}

}
