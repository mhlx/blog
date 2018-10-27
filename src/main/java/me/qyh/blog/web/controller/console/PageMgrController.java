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
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("console/template/page")
public class PageMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private SpaceService spaceService;

	@GetMapping
	public String index() {
		return "console/template/page";
	}

	@GetMapping(value = "new")
	public String build(Model model) {
		model.addAttribute("page", new Page());
		SpaceQueryParam param = new SpaceQueryParam();
		model.addAttribute("spaces", spaceService.querySpace(param));
		return "console/template/page_build";
	}

	@GetMapping(value = "edit/{id}")
	public String update(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		Optional<Page> optional = templateService.queryPage(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("page.user.notExists", "自定义页面不存在"));
			return "redirect:/console/template/page";
		}
		Page page = optional.get();
		model.addAttribute("page", page);
		SpaceQueryParam param = new SpaceQueryParam();
		model.addAttribute("spaces", spaceService.querySpace(param));
		return "console/template/page_build";
	}

}
