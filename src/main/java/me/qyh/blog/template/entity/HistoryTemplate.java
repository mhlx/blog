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
