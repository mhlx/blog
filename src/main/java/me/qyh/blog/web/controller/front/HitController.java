package me.qyh.blog.web.controller.front;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.util.UrlUtils;

@RestController
@RequestMapping("api")
public class HitController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private NewsService newsService;

	@PatchMapping("space/{alias}/article/{id}")
	public ResponseEntity<Void> hitArticle(@PathVariable("id") Integer id, @RequestHeader("referer") String referer) {
		try {
			UriComponents uc = UriComponentsBuilder.fromHttpUrl(referer).build();
			if (!UrlUtils.match("/space/" + Environment.getSpaceAlias() + "/article/*", uc.getPath())
					&& !UrlUtils.match("/article/*", uc.getPath())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		articleService.hit(id);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("news/{id}")
	public ResponseEntity<Void> hitNews(@PathVariable("id") Integer id, @RequestHeader("referer") String referer) {
		try {
			UriComponents uc = UriComponentsBuilder.fromHttpUrl(referer).build();
			if (!UrlUtils.match("/news/*", uc.getPath())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		newsService.hit(id);
		return ResponseEntity.ok().build();
	}

}
