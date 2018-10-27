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
package me.qyh.blog.web.controller.front;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.template.render.Fragments;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRender;
import me.qyh.blog.template.render.TemplateRenderException;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.FragmentValidator;
import me.qyh.blog.template.vo.DataBind;
import me.qyh.blog.template.vo.DataTag;

@RestController
@RequestMapping("api")
public class OtherController {

	@Autowired
	private TemplateRender templateRender;
	@Autowired
	private TemplateService templateService;

	@GetMapping({ "data/{tagName}", "space/{alias}/data/{tagName}" })
	public ResponseEntity<Map<String, Object>> queryData(@PathVariable("tagName") String tagName,
			@RequestParam Map<String, String> allRequestParams, HttpServletRequest request,
			HttpServletResponse response) throws LogicException {
		try {
			tagName = URLDecoder.decode(tagName, Constants.CHARSET.name());
		} catch (UnsupportedEncodingException e) {
			throw new LogicException("data.name.undecode", "无法解码的数据名称");
		}
		DataTag tag = new DataTag(tagName, new HashMap<>(allRequestParams));
		Optional<DataBind> op = templateService.queryData(tag, true);
		if (op.isPresent()) {
			DataBind bind = op.get();
			Object data = bind.getData();
			if (data == null) {
				return ResponseEntity.ok(Map.of("dataName", bind.getDataName()));
			} else {
				return ResponseEntity.ok(Map.of("dataName", bind.getDataName(), "data", data));
			}
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping({ "fragment/{fragment}", "space/{alias}/fragment/{fragment}" })
	public ResponseEntity<String> queryFragment(@PathVariable("fragment") String fragment,
			@RequestParam Map<String, String> allRequestParams, HttpServletRequest request,
			HttpServletResponse response) throws LogicException, TemplateRenderException {
		try {
			fragment = FragmentValidator.validName(fragment, true);
		} catch (LogicException e) {
			throw new LogicException("data.name.undecode", "无法解码的数据名称");
		}
		try {

			String templateName = Fragments.getCurrentTemplateName(fragment);
			String content = templateRender.doRender(templateName, null, request, new ReadOnlyResponse(response),
					new ParseConfig(true));
			return ResponseEntity.ok(content);

		} catch (TemplateRenderException e) {
			throw e;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new SystemException(e.getMessage(), e);
		}
	}
}
