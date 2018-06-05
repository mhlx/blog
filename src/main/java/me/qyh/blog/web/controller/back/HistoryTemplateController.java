/*
 * Copyright 2018 qyh.me
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("mgr/template/history")
public class HistoryTemplateController extends BaseMgrController {

	private static final int MAX_REMARK_LENGTH = 500;

	@Autowired
	private TemplateService templateService;

	@GetMapping("get/{id}")
	@ResponseBody
	public JsonResult getHistoryTemplate(@PathVariable("id") Integer id) {
		return templateService.getHistoryTemplate(id).map(historyTemplate -> new JsonResult(true, historyTemplate))
				.orElse(new JsonResult(false, new Message("historyTemplate.notExists", "历史模板不存在")));
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam("id") Integer id) throws LogicException {
		templateService.deleteHistoryTemplate(id);
		return new JsonResult(true, new Message("historyTemplate.delete.success", "删除成功"));
	}

	@PostMapping("update")
	@ResponseBody
	public JsonResult update(@RequestParam("id") Integer id, @RequestParam("remark") String remark)
			throws LogicException {
		Optional<Message> optionalError = validRemark(remark);
		if (optionalError.isPresent()) {
			return new JsonResult(false, optionalError.get());
		}
		return new JsonResult(true, templateService.updateHistoryTemplate(id, remark));
	}

	/**
	 * 校验模板备注，
	 * 
	 * @param remark
	 * @return 错误信息
	 */
	static Optional<Message> validRemark(String remark) {
		Message message = null;
		if (remark == null) {
			message = new Message("historyTemplate.remark.blank", "备注不能为空");
		} else if (remark.length() > MAX_REMARK_LENGTH) {
			message = new Message("historyTemplate.remark.toolong", "备注不能超过" + MAX_REMARK_LENGTH + "个字符",
					MAX_REMARK_LENGTH);
		}
		return Optional.ofNullable(message);
	}

}
