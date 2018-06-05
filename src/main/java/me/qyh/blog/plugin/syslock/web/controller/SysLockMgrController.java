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

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.plugin.syslock.component.SysLockProvider;
import me.qyh.blog.plugin.syslock.entity.PasswordLock;
import me.qyh.blog.plugin.syslock.entity.QALock;
import me.qyh.blog.plugin.syslock.validator.PasswordLockValidator;
import me.qyh.blog.plugin.syslock.validator.QALockValidator;
import me.qyh.blog.web.controller.back.BaseMgrController;

@Controller
@RequestMapping("mgr/lock/sys")
public class SysLockMgrController extends BaseMgrController {

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

	@GetMapping("get/{id}")
	@ResponseBody
	public JsonResult lock(@PathVariable("id") String id) {
		return sysLockProvider.getLock(id).map(sysLock -> new JsonResult(true, sysLock)).orElse(new JsonResult(false));
	}

	@PostMapping("qa/add")
	@ResponseBody
	public JsonResult addQALock(@Validated @RequestBody QALock lock) {
		sysLockProvider.addLock(lock);
		return new JsonResult(true, new Message("lock.add.success", "添加成功"));
	}

	@PostMapping("password/add")
	@ResponseBody
	public JsonResult addPasswordLock(@Validated @RequestBody PasswordLock lock) {
		sysLockProvider.addLock(lock);
		return new JsonResult(true, new Message("lock.add.success", "添加成功"));
	}

	@PostMapping("qa/update")
	@ResponseBody
	public JsonResult updateQALock(@Validated @RequestBody QALock lock) throws LogicException {
		sysLockProvider.updateLock(lock);
		return new JsonResult(true, new Message("lock.update.success", "更新成功"));
	}

	@PostMapping("password/update")
	@ResponseBody
	public JsonResult updatePasswordLock(@Validated @RequestBody PasswordLock lock) throws LogicException {
		sysLockProvider.updateLock(lock);
		return new JsonResult(true, new Message("lock.update.success", "更新成功"));
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult deleteLock(@RequestParam("id") String id) {
		sysLockProvider.removeLock(id);
		return new JsonResult(true, new Message("lock.delete.success", "删除成功"));
	}

	@GetMapping("index")
	public String index(Model model) {
		model.addAttribute("locks", sysLockProvider.getAllLocks());
		return "plugin/syslock/index";
	}

}
