package me.qyh.blog.template.render;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TemplateRenderErrorDescription implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<TemplateErrorInfo> templateErrorInfos = new ArrayList<>();
	private String expression;// 表达式
	private String stackTrace;

	public TemplateRenderErrorDescription() {
		super();
	}

	public List<TemplateErrorInfo> getTemplateErrorInfos() {
		return templateErrorInfos;
	}

	public void addTemplateErrorInfos(TemplateErrorInfo templateErrorInfo) {
		this.templateErrorInfos.add(templateErrorInfo);
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public static class TemplateErrorInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String templateName;
		private final Integer line;
		private final Integer col;

		public TemplateErrorInfo(String templateName, Integer line, Integer col) {
			super();
			this.templateName = templateName;
			this.line = line;
			this.col = col;
		}

		public String getTemplateName() {
			return templateName;
		}

		public Integer getLine() {
			return line;
		}

		public Integer getCol() {
			return col;
		}

	}

}
