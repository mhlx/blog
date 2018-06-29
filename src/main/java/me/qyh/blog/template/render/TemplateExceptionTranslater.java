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

import java.util.Optional;

/**
 * 用来将模板异常转化为可读的{@code TemplateRenderException}
 * 
 */
public interface TemplateExceptionTranslater {

	/**
	 * 转化异常
	 * 
	 * @param templateName
	 *            模板名称
	 * @param e
	 *            异常
	 * @return 模板渲染异常
	 */
	Optional<TemplateRenderException> translate(String templateName, Throwable e);

}
