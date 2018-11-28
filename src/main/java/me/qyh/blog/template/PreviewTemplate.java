package me.qyh.blog.template;

import java.util.Optional;

/**
 * 预览模板
 * 
 * @author wwwqyhme
 *
 */
public final class PreviewTemplate implements Template {
	private final Template template;

	/**
	 * 预览模板前缀
	 */
	private static final String TEMPLATE_PREVIEW_PREFIX = TEMPLATE_PREFIX + "Preview" + SPLITER;

	public Template getOriginalTemplate() {
		return template;
	}

	public PreviewTemplate(Template template) {
		super();
		this.template = template;
	}

	@Override
	public String getTemplate() {
		return template.getTemplate();
	}

	@Override
	public String getTemplateName() {
		return TEMPLATE_PREVIEW_PREFIX + template.getTemplateName();
	}

	@Override
	public Template cloneTemplate() {
		return new PreviewTemplate(template);
	}

	@Override
	public boolean isCallable() {
		return template.isCallable();
	}

	@Override
	public boolean equalsTo(Template other) {
		return false;
	}

	@Override
	public boolean cacheable() {
		return false;
	}

	/**
	 * 判断是否是预览模板文件名
	 * 
	 * @param templateName
	 * @return
	 */
	public static boolean isPreviewTemplate(String templateName) {
		return templateName != null && templateName.startsWith(TEMPLATE_PREVIEW_PREFIX);
	}

	public static String getTemplateName(String templateName) {
		if (isPreviewTemplate(templateName)) {
			return templateName;
		}
		return TEMPLATE_PREVIEW_PREFIX + templateName;
	}

	public static Optional<String> getOriginalTemplateName(String previewTemplateName) {
		if (isPreviewTemplate(previewTemplateName)) {
			return Optional.of(previewTemplateName.substring(TEMPLATE_PREVIEW_PREFIX.length()));
		}
		return Optional.empty();
	}
}
