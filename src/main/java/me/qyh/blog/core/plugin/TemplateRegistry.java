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
package me.qyh.blog.core.plugin;

/**
 * 模板注册
 * <p>
 * <b>仅用于插件！！！</b>
 * </p>
 * 
 * @author wwwqyhme
 *
 */
public interface TemplateRegistry {

	/**
	 * 注册为系统模板
	 * <p>
	 * 如果路径已经存在，则替换，如果系统模板不存在，则增加新的系统模板
	 * </p>
	 * 
	 * @param path
	 * @param template
	 * @return
	 */
	TemplateRegistry registerSystemTemplate(String path, String template);

	/**
	 * 注册全局默认模板片段
	 * <p>
	 * 如果模板片段已经存在，则替换，否则增加新的模板片段
	 * </p>
	 * 
	 * @param name
	 * @param template
	 * @param callable
	 * @return
	 */
	TemplateRegistry registerGlobalFragment(String name, String template, boolean callable);

}
