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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.TagService;
import me.qyh.blog.core.validator.TagQueryParamValidator;
import me.qyh.blog.core.validator.TagValidator;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.TagQueryParam;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RequestMapping("api/console")
@RestController
public class TagConsole extends BaseMgrController {

	@Autowired
	private TagService tagService;
	@Autowired
	private TagValidator tagValidator;
	@Autowired
	private TagQueryParamValidator tagQueryParamValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "tag")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(tagValidator);
	}

	@InitBinder(value = "tagQueryParam")
	protected void initTagQueryParamBinder(WebDataBinder binder) {
		binder.setValidator(tagQueryParamValidator);
	}

	@GetMapping("tags")
	public PageResult<Tag> index(@Validated TagQueryParam tagQueryParam) {
		tagQueryParam.setPageSize(configServer.getGlobalConfig().getTagPageSize());
		return tagService.queryTag(tagQueryParam);
	}

	@DeleteMapping("tag/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		tagService.deleteTag(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("tag/{id}")
	public ResponseEntity<Void> update(@RequestBody @Validated Tag tag, @PathVariable("id") Integer id,
			@RequestParam(defaultValue = "false", required = false) boolean merge) throws LogicException {
		tag.setId(id);
		tagService.updateTag(tag, merge);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("tag/{id}")
	public ResponseEntity<Tag> getTag(@PathVariable("id") Integer id) {
		return ResponseEntity.of(tagService.getTag(id));
	}

}
