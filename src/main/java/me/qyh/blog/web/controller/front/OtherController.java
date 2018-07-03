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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.PreviewTemplate;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRender;
import me.qyh.blog.template.render.TemplateRenderException;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.FragmentValidator;
import me.qyh.blog.template.vo.DataBind;
import me.qyh.blog.template.vo.DataTag;
import me.qyh.blog.web.Webs;

@Controller
public class OtherController {

	@Autowired
	private TemplateRender templateRender;
	@Autowired
	private TemplateService templateService;

	@GetMapping({ "data/{tagName}", "space/{alias}/data/{tagName}" })
	@ResponseBody
	public JsonResult queryData(@PathVariable("tagName") String tagName,
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
			return new JsonResult(true, Map.of("dataName", bind.getDataName(), "data", data));
		} else {
			return new JsonResult(false);
		}
	}

	@GetMapping({ "fragment/{fragment}", "space/{alias}/fragment/{fragment}" })
	public void queryFragment(@PathVariable("fragment") String fragment,
			@RequestParam Map<String, String> allRequestParams, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		try {
			fragment = FragmentValidator.validName(fragment, true);
		} catch (LogicException e) {
			Webs.writeInfo(response, new JsonResult(false, e.getLogicMessage()));
			return;
		}
		try {

			String templateName = Fragment.getTemplateName(fragment, Environment.getSpace());
			if (templateService.isPreviewIp(Environment.getIP())) {
				templateName = PreviewTemplate.getTemplateName(templateName);
			}

			String content = templateRender.doRender(templateName, null, request, new ReadOnlyResponse(response),
					new ParseConfig(true));

			Webs.writeInfo(response, new JsonResult(true, content));

		} catch (TemplateRenderException e) {
			Webs.writeInfo(response, new JsonResult(false, e.getRenderErrorDescription()));
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new SystemException(e.getMessage(), e);
		}
	}
}
