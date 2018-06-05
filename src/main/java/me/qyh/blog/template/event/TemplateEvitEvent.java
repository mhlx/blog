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
package me.qyh.blog.template.event;

import org.springframework.context.ApplicationEvent;

import me.qyh.blog.core.util.Validators;

/**
 * 用来监听template缓存清除事件
 * 
 * <p>
 * <b>这个事件仅应该在事务完成后发布</b>
 * </p>
 * 
 * @author Administrator
 *
 */
public class TemplateEvitEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String[] templateNames;

	public TemplateEvitEvent(Object source, String... templateNames) {
		super(source);
		this.templateNames = templateNames;
	}

	public String[] getTemplateNames() {
		return templateNames;
	}

	public boolean clear() {
		return Validators.isEmpty(templateNames);
	}

}
