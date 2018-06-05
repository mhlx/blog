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
package me.qyh.blog.template.entity;

import java.sql.Timestamp;

import me.qyh.blog.core.entity.BaseEntity;
import me.qyh.blog.template.Template;

/**
 * @since 2017/12/27
 * @author wwwqyhme
 *
 */
public class HistoryTemplate extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String templateName;
	private String tpl;// 模板
	private Timestamp time;
	private String remark;

	public HistoryTemplate() {
		super();
	}

	public HistoryTemplate(Template template) {
		this.templateName = template.getTemplateName();
		this.tpl = template.getTemplate();
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTpl() {
		return tpl;
	}

	public void setTpl(String tpl) {
		this.tpl = tpl;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
