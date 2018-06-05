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
package me.qyh.blog.template.render;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.exception.LogicException;

/**
 * 渲染模板内容
 */
public interface TemplateRenderExecutor {

	/**
	 * 
	 * @param viewTemplateName
	 * @param model
	 *            额外参数
	 * @param request
	 *            当前请求
	 * @param readOnlyResponse
	 *            <b>READ ONLY</b> response
	 * @return
	 * @throws Exception
	 */
	String execute(String viewTemplateName, Map<String, Object> model, HttpServletRequest request,
			ReadOnlyResponse readOnlyResponse) throws Exception;

	/**
	 * 判断是否支持pjax，并且从原始模板名获取新的模板名称
	 * 
	 * @since 6.4
	 * @param templateName
	 * @param container
	 *            X-PJAX-Container value <b> 可能为null </b>
	 * @return
	 */
	default String processPjaxTemplateName(String templateName, HttpServletRequest request) throws LogicException {
		throw new LogicException("templateRender.pjax.unsupport", "不支持pjax的处理");
	}

}
