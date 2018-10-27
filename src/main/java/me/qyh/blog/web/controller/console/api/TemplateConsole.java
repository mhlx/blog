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
package me.qyh.blog.web.controller.console.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.ExportPagesValidator;
import me.qyh.blog.template.vo.DataTagProcessorBean;
import me.qyh.blog.template.vo.ExportPages;
import me.qyh.blog.template.vo.ImportRecord;
import me.qyh.blog.template.vo.PreviewImport;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console/template")
public class TemplateConsole extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	private ExportPagesValidator exportPagesValidator;

	@InitBinder(value = "exportPages")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(exportPagesValidator);
	}

	@DeleteMapping("caches")
	public ResponseEntity<Void> clearPageCache() {
		templateEngine.getConfiguration().getTemplateManager().clearCaches();
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("previews")
	public ResponseEntity<Void> clearPreview() {
		templateService.clearPreview();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("datas")
	public List<DataTagProcessorBean> allDatas() {
		return templateService.queryDataTags();
	}

	@PatchMapping("data/{name}")
	public ResponseEntity<Void> updateCallable(@PathVariable("name") String name,
			@RequestParam("callable") boolean callable) {
		templateService.updateDataCallable(name, callable);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("imports")
	public ResponseEntity<List<ImportRecord>> importPage(@Validated @RequestBody ExportPages exportPages) {
		return ResponseEntity.ok(templateService.importPage(exportPages));
	}

	@PostMapping("imports/preview")
	@ResponseBody
	public ResponseEntity<PreviewImport> previewImportPage(@Validated @RequestBody ExportPages exportPages)
			throws LogicException {
		return ResponseEntity.ok(templateService.previewImport(exportPages));
	}
}
