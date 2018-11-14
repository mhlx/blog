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

	public enum HistoryTemplateType {
		PAGE, FRAGMENT;
	}

	private Integer templateId;
	private HistoryTemplateType type;
	private String tpl;// 模板
	private Timestamp time;
	private String remark;

	public HistoryTemplate() {
		super();
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public HistoryTemplateType getType() {
		return type;
	}

	public void setType(HistoryTemplateType type) {
		this.type = type;
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
