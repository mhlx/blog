package me.qyh.blog.template.render;

import me.qyh.blog.core.util.ExceptionUtils;

public class TemplateRenderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String templateName;
	private final TemplateRenderErrorDescription renderErrorDescription;
	private final boolean fromPreview;

	public TemplateRenderException(String templateName, TemplateRenderErrorDescription description, Throwable ex,
			boolean fromPreview) {
		super(null, ex, true, false);
		this.renderErrorDescription = description;
		this.templateName = templateName;
		this.fromPreview = fromPreview;
	}

	public TemplateRenderErrorDescription getRenderErrorDescription() {
		return renderErrorDescription;
	}

	/**
	 * root template name
	 * 
	 * @return
	 */
	public String getTemplateName() {
		return templateName;
	}

	public boolean isFromPreview() {
		return fromPreview;
	}

	/**
	 * 将错误栈写入 {@code TemplateRenderErrorDescription}
	 * 
	 * @see TemplateRenderErrorDescription#getStackTrace()
	 */
	public void writeStackTrace() {
		renderErrorDescription.setStackTrace(ExceptionUtils.getStackTrace(this));
	}

}
