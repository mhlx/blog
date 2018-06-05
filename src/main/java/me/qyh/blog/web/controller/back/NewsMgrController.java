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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.validator.NewsQueryParamValidator;
import me.qyh.blog.core.validator.NewsValidator;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.NewsQueryParam;

@Controller
@RequestMapping("mgr/news")
public class NewsMgrController extends BaseMgrController {
	@Autowired
	private NewsValidator newsValidator;
	@Autowired
	private NewsService newsService;
	@Autowired
	private NewsQueryParamValidator newsQueryParamValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "news")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(newsValidator);
	}

	@InitBinder(value = "newsQueryParam")
	protected void initQueryBinder(WebDataBinder binder) {
		binder.setValidator(newsQueryParamValidator);
	}

	@GetMapping("index")
	public String index(@Validated NewsQueryParam newsQueryParam, Model model) {
		newsQueryParam.setQueryPrivate(true);
		newsQueryParam.setPageSize(configServer.getGlobalConfig().getNewsPageSize());
		model.addAttribute("page", newsService.queryNews(newsQueryParam));
		return "mgr/news/index";
	}

	@PostMapping("del/{id}")
	@ResponseBody
	public JsonResult del(@PathVariable("id") Integer id) throws LogicException {
		newsService.deleteNews(id);
		return new JsonResult(true, "删除成功");
	}

	@GetMapping("write")
	public String write(Model model) {
		model.addAttribute("news", new News());
		return "mgr/news/write";
	}

	@PostMapping("write")
	@ResponseBody
	public JsonResult write(@Validated @RequestBody News news) throws LogicException {
		newsService.saveNews(news);
		return new JsonResult(true, new Message("news.save.success", "保存成功"));
	}

	@GetMapping("update/{id}")
	public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		Optional<News> op = newsService.getNews(id);
		if (op.isPresent()) {
			model.addAttribute("news", op.get());
			return "mgr/news/write";
		}
		ra.addFlashAttribute("error", new Message("news.notExists", "动态不存在"));
		return "redirect:/mgr/news/index";
	}

	@PostMapping("update")
	@ResponseBody
	public JsonResult update(@Validated @RequestBody News news) throws LogicException {
		newsService.updateNews(news);
		return new JsonResult(true, new Message("news.update.success", "更新成功"));
	}
}
