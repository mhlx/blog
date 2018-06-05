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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("mgr/template/data")
public class TemplateDataMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;

	@GetMapping("index")
	public String index(Model model) {
		model.addAttribute("datas", templateService.queryDataTags());
		return "mgr/template/data";
	}

	@PostMapping("updateCallable")
	@ResponseBody
	public JsonResult updateCallable(@RequestParam("name") String name, @RequestParam("callable") boolean callable) {
		templateService.updateDataCallable(name, callable);
		return new JsonResult(true, new Message("template.data.update.success", "更新成功"));
	}
}
