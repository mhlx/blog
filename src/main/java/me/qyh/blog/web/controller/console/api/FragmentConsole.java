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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.entity.HistoryTemplate;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.FragmentQueryParamValidator;
import me.qyh.blog.template.validator.FragmentValidator;
import me.qyh.blog.template.vo.FragmentQueryParam;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console/template")
public class FragmentConsole extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private FragmentQueryParamValidator fragmentParamValidator;
	@Autowired
	private FragmentValidator fragmentValidator;
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

	@GetMapping("fragments")
	public PageResult<Fragment> findFragments(@Validated FragmentQueryParam fragmentQueryParam, Model model) {
		fragmentQueryParam.setPageSize(configServer.getGlobalConfig().getFragmentPageSize());
		return templateService.queryFragment(fragmentQueryParam);
	}

	@PostMapping("fragment")
	public ResponseEntity<Fragment> create(@RequestBody @Validated final Fragment fragment) throws LogicException {
		if (fragment.isGlobal()) {
			fragment.setSpace(null);
		}
		Fragment created = templateService.insertFragment(fragment);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@DeleteMapping("fragment/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		templateService.deleteFragment(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("fragment/{id}")
	public ResponseEntity<Void> update(@RequestBody @Validated Fragment fragment, @PathVariable("id") Integer id)
			throws LogicException {
		fragment.setId(id);
		templateService.updateFragment(fragment);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("fragment/{id}")
	public ResponseEntity<Fragment> get(@PathVariable("id") Integer id) {
		Optional<Fragment> op = templateService.queryFragment(id);
		return ResponseEntity.of(op);
	}

	@GetMapping("fragment/{id}/histories")
	public List<HistoryTemplate> getHistories(@PathVariable("id") Integer id) throws LogicException {
		return templateService.queryFragmentHistory(id);
	}

	@PostMapping("fragment/{id}/history")
	public ResponseEntity<Void> saveHistory(@PathVariable("id") Integer id, @RequestParam("remark") String remark)
			throws LogicException {
		Optional<Message> optionalError = HistoryTemplateConsole.validRemark(remark);
		if (optionalError.isPresent()) {
			throw new LogicException(optionalError.get());
		}
		templateService.saveFragmentHistory(id, remark);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("defaultFragments")
	public List<Fragment> getDefaul() {
		return templateService.getDefaultFragment();
	}

	@PostMapping("fragment/preview")
	@ResponseBody
	public ResponseEntity<Void> preview(@RequestBody @Validated Fragment fragment) throws LogicException {
		templateService.registerPreview(fragment);
		return ResponseEntity.noContent().build();
	}
}
