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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.FragmentQueryParamValidator;
import me.qyh.blog.template.validator.FragmentValidator;
import me.qyh.blog.template.vo.FragmentQueryParam;

@Controller
@RequestMapping("mgr/template/fragment")
public class FragmentMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private FragmentQueryParamValidator fragmentParamValidator;
	@Autowired
	private FragmentValidator fragmentValidator;
	@Autowired
	private SpaceService spaceService;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "fragmentQueryParam")
	protected void initFragmentQueryParamBinder(WebDataBinder binder) {
		binder.setValidator(fragmentParamValidator);
	}

	@InitBinder(value = "fragment")
	protected void initFragmentBinder(WebDataBinder binder) {
		binder.setValidator(fragmentValidator);
	}

	@GetMapping("index")
	public String index(@Validated FragmentQueryParam fragmentQueryParam, Model model) {
		fragmentQueryParam.setPageSize(configServer.getGlobalConfig().getFragmentPageSize());
		model.addAttribute("page", templateService.queryFragment(fragmentQueryParam));
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "mgr/template/fragment";
	}

	@GetMapping("list")
	@ResponseBody
	public JsonResult listJson(@Validated FragmentQueryParam fragmentQueryParam, Model model) {
		fragmentQueryParam.setPageSize(configServer.getGlobalConfig().getFragmentPageSize());
		return new JsonResult(true, templateService.queryFragment(fragmentQueryParam));
	}

	@PostMapping("create")
	@ResponseBody
	public JsonResult create(@RequestBody @Validated final Fragment fragment) throws LogicException {
		if (fragment.isGlobal()) {
			fragment.setSpace(null);
		}
		Fragment created = templateService.insertFragment(fragment);
		return new JsonResult(true, created);
	}

	@GetMapping("new")
	public String newFragment(Model model) {
		model.addAttribute("fragment", new Fragment());
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "mgr/template/fragment_build";
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam("id") Integer id) throws LogicException {
		templateService.deleteFragment(id);
		return new JsonResult(true, new Message("fragment.user.delete.success", "删除成功"));
	}

	@PostMapping("update")
	@ResponseBody
	public JsonResult update(@RequestBody @Validated final Fragment fragment) throws LogicException {
		if (fragment.isGlobal()) {
			fragment.setSpace(null);
		}
		return new JsonResult(true, templateService.updateFragment(fragment));
	}

	@GetMapping(value = "update")
	public String update(@RequestParam("id") Integer id, Model model, RedirectAttributes ra) {
		Optional<Fragment> optional = templateService.queryFragment(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("fragment.user.notExists", "自定义模板片段不存在"));
			return "redirect:/mgr/template/fragment/index";
		}
		Fragment fragment = optional.get();
		model.addAttribute("fragment", fragment);
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "mgr/template/fragment_build";
	}

	@GetMapping("get/{id}")
	@ResponseBody
	public JsonResult get(@PathVariable("id") Integer id) {
		return templateService.queryFragment(id).map(fragment -> new JsonResult(true, fragment))
				.orElse(new JsonResult(false));
	}

	@GetMapping("{id}/history")
	@ResponseBody
	public JsonResult getHistory(@PathVariable("id") Integer id) {
		return new JsonResult(true, templateService.queryFragmentHistory(id));
	}

	@PostMapping("{id}/saveHistory")
	@ResponseBody
	public JsonResult saveHistory(@PathVariable("id") Integer id, @RequestParam("remark") String remark)
			throws LogicException {
		Optional<Message> optionalError = HistoryTemplateController.validRemark(remark);
		if (optionalError.isPresent()) {
			return new JsonResult(false, optionalError.get());
		}
		templateService.saveFragmentHistory(id, remark);
		return new JsonResult(true, new Message("historyTemplate.save.success", "保存成功"));
	}

	@PostMapping("preview")
	@ResponseBody
	public JsonResult preview(@RequestBody @Validated Fragment fragment) throws LogicException {
		Space space = fragment.getSpace();
		if (space != null) {
			space = spaceService.getSpace(space.getId()).orElse(null);
		}
		fragment.setSpace(space);

		templateService.registerPreview(fragment);

		return new JsonResult(true);
	}

	@GetMapping("default")
	@ResponseBody
	public JsonResult getDefaul() {
		return new JsonResult(true, templateService.getDefaultFragment());
	}
}
