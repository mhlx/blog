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
