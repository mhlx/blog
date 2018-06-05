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
package me.qyh.blog.template;

import java.io.Serializable;

/**
 * 模板
 * 
 * @author Administrator
 *
 */
public interface Template extends Serializable {

	/**
	 * 模板分割符
	 */
	String SPLITER = "%";

	/**
	 * 模板前缀，所有的模板名必须以这个开头
	 */
	String TEMPLATE_PREFIX = "Template" + SPLITER;

	/**
	 * 判断是否是模板文件名
	 * 
	 * @param templateName
	 * @return
	 */
	static boolean isTemplate(String templateName) {
		return templateName != null && templateName.startsWith(TEMPLATE_PREFIX);
	}

	/**
	 * 获取模板内容
	 * 
	 * @return
	 */
	String getTemplate();

	/**
	 * 获取模板名称
	 * <p>
	 * 模板名称应该是全局唯一的
	 * </p>
	 * 
	 * @return
	 */
	String getTemplateName();

	/**
	 * 克隆 template
	 * 
	 * @return
	 */
	Template cloneTemplate();

	/**
	 * 是否可被外部调用
	 * 
	 * @return
	 */
	boolean isCallable();

	/**
	 * 判断是否和另一个Template等价
	 * 
	 * @return
	 */
	boolean equalsTo(Template other);

	/**
	 * 是否可以被缓存
	 * 
	 * @return
	 */
	default boolean cacheable() {
		return true;
	}
}
