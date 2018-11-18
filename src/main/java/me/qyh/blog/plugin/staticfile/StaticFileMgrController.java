/*
 * Copyright 2017 qyh.me
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
package me.qyh.blog.plugin.staticfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.plugin.staticfile.vo.FileContent;
import me.qyh.blog.web.controller.console.BaseMgrController;

@EnsureLogin
@Controller
@RequestMapping("console/staticFile")
public class StaticFileMgrController extends BaseMgrController {

	@Autowired
	private StaticFileManager handler;

	@GetMapping
	public String index() {
		return "plugin/staticfile/index";
	}

	@GetMapping("edit")
	public String edit(@RequestParam("path") String path, Model model, RedirectAttributes ra) {
		try {
			FileContent fileContent = handler.getEditableFile(path);
			model.addAttribute("file", fileContent);

			String ext = fileContent.getExt().toLowerCase();
			if (ext.equals("htm")) {
				ext = "html";
			}
			return "plugin/staticfile/editor_" + ext;
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/console/staticFile";
		}
	}
}
