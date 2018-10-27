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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.SpaceValidator;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class SpaceConsole extends BaseMgrController {

	@Autowired
	private SpaceService spaceService;
	@Autowired
	private SpaceValidator spaceValidator;

	@InitBinder(value = "space")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(spaceValidator);
	}

	@GetMapping("spaces")
	public List<Space> index(SpaceQueryParam spaceQueryParam) {
		return spaceService.querySpace(spaceQueryParam);
	}

	@PostMapping("space")
	public ResponseEntity<Void> add(@RequestBody @Validated Space space) throws LogicException {
		if (Validators.isEmptyOrNull(space.getLockId(), true)) {
			space.setLockId(null);
		}
		spaceService.addSpace(space);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("space/{id}")
	public ResponseEntity<Void> update(@RequestBody @Validated Space space, @PathVariable("id") Integer id)
			throws LogicException {
		if (Validators.isEmptyOrNull(space.getLockId(), true)) {
			space.setLockId(null);
		}
		space.setId(id);
		spaceService.updateSpace(space);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("space/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		spaceService.deleteSpace(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("space/{id}")
	public ResponseEntity<Space> get(@PathVariable("id") Integer id) {
		return ResponseEntity.of(spaceService.getSpace(id));
	}
}
