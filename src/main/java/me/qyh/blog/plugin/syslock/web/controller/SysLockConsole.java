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
package me.qyh.blog.plugin.syslock.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.syslock.component.SysLockProvider;
import me.qyh.blog.plugin.syslock.entity.PasswordLock;
import me.qyh.blog.plugin.syslock.entity.QALock;
import me.qyh.blog.plugin.syslock.validator.PasswordLockValidator;
import me.qyh.blog.plugin.syslock.validator.QALockValidator;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class SysLockConsole extends BaseMgrController {

	@Autowired
	private SysLockProvider sysLockProvider;
	@Autowired
	private QALockValidator qaLockValidator;
	@Autowired
	private PasswordLockValidator passwordLockValidator;

	@InitBinder(value = "qaLock")
	protected void initQALockBinder(WebDataBinder binder) {
		binder.setValidator(qaLockValidator);
	}

	@InitBinder(value = "passwordLock")
	protected void initPasswordLockBinder(WebDataBinder binder) {
		binder.setValidator(passwordLockValidator);
	}

	@GetMapping("syslock/{id}")
	public ResponseEntity<Lock> lock(@PathVariable("id") String id) {
		return ResponseEntity.of(sysLockProvider.getLock(id));
	}

	@PostMapping("syslock/qa")
	public ResponseEntity<Void> addQALock(@Validated @RequestBody QALock lock) {
		sysLockProvider.addLock(lock);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("syslock/password")
	public ResponseEntity<Void> addPasswordLock(@Validated @RequestBody PasswordLock lock) {
		sysLockProvider.addLock(lock);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("syslock/qa/{id}")
	public ResponseEntity<Void> updateQALock(@Validated @RequestBody QALock lock, @PathVariable("id") String id)
			throws LogicException {
		lock.setId(id);
		sysLockProvider.updateLock(lock);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("syslock/password/{id}")
	public ResponseEntity<Void> updatePasswordLock(@Validated @RequestBody PasswordLock lock,
			@PathVariable("id") String id) throws LogicException {
		lock.setId(id);
		sysLockProvider.updateLock(lock);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("syslock/{id}")
	public ResponseEntity<Void> deleteLock(@PathVariable("id") String id) {
		sysLockProvider.removeLock(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("syslocks")
	public List<Lock> getAllSysLocks() {
		return sysLockProvider.getAllLocks();
	}
}
