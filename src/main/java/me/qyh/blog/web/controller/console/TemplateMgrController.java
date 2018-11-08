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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.ExportPagesValidator;
import me.qyh.blog.template.vo.ExportPage;

@Controller
@RequestMapping("console/template")
public class TemplateMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private SpaceService spaceService;
	@Autowired
	private ExportPagesValidator exportPagesValidator;

	@InitBinder(value = "exportPages")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(exportPagesValidator);
	}

	@PostMapping("export")
	public Object export(@RequestParam(value = "spaceId", required = false) Integer spaceId, RedirectAttributes ra) {
		try {
			List<ExportPage> pages = templateService.exportPage(spaceId);
			return download(pages, spaceId == null ? null
					: spaceService.getSpace(spaceId).orElseThrow(() -> new SystemException("空间" + spaceId + "不存在")));
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/console/template/index";
		}
	}

	@GetMapping
	public String index(Model model) {
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "console/template/index";
	}

	@GetMapping("data")
	public String data() {
		return "console/template/data";
	}

	private ResponseEntity<byte[]> download(List<ExportPage> pages, Space space) {
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String filenamePrefix = "";
		if (space != null) {
			filenamePrefix += space.getAlias() + "-";
		}
		filenamePrefix += Times.format(Times.now(), "yyyyMMddHHmmss");
		header.set("Content-Disposition", "attachment; filename=" + filenamePrefix + ".json");
		return new ResponseEntity<>(Jsons.write(pages).getBytes(Constants.CHARSET), header, HttpStatus.OK);
	}
}
