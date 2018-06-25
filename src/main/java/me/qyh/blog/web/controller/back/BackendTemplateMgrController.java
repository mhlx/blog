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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.BackendTemplate;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("mgr/template/backend")
@EnsureLogin
public class BackendTemplateMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;

	private static final int MAX_CONTENT_LENGTH = 200000;

	@GetMapping("index")
	public String index() {
		return "mgr/template/backend";
	}

	@GetMapping("paths")
	@ResponseBody
	public JsonResult query() {
		return new JsonResult(true, templateService.getAllBackendTemplateInfos());
	}

	@GetMapping("edit")
	public String edit(@RequestParam("path") String path, RedirectAttributes ra, Model model) {
		Optional<BackendTemplate> op = templateService.getBackendTemplate(path);
		if (op.isPresent()) {
			model.addAttribute("template", op.get());
			return "mgr/template/backend_build";
		} else {
			ra.addFlashAttribute("error", new Message("template.backend.notExists", "管理台模板路径不存在"));
			return "redirect:/mgr/template/backend/index";
		}
	}

	@PostMapping("edit")
	@ResponseBody
	public JsonResult edit(@RequestParam("path") String path, @RequestParam("content") String content)
			throws LogicException {
		if (Validators.isEmptyOrNull(content, true)) {
			return new JsonResult(false, new Message("template.backend.content.blank", "模板内容不能为空"));
		}
		if (content.length() > MAX_CONTENT_LENGTH) {
			return new JsonResult(false, new Message("template.backend.content.toolong",
					"模板内容不能超过" + MAX_CONTENT_LENGTH + "个字符", MAX_CONTENT_LENGTH));
		}
		templateService.editBackendTemplate(path, content);
		return new JsonResult(true, new Message("template.backend.edit.success", "编辑成功"));
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam("path") String path) {
		templateService.deleteBackendTemplate(path);
		return new JsonResult(true, new Message("template.backend.delete.success", "删除成功"));
	}
}
