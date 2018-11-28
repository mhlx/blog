package me.qyh.blog.template.render;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.exception.LogicException;

/**
 * 渲染模板内容
 */
public interface TemplateRenderExecutor {

	/**
	 * @param viewTemplateName
	 * @param model
	 *            额外参数
	 * @param request
	 *            当前请求
	 * @param readOnlyResponse
	 *            <b>READ ONLY</b> response
	 * @return
	 * @throws Exception
	 */
	String execute(String viewTemplateName, Map<String, Object> model, HttpServletRequest request,
			ReadOnlyResponse readOnlyResponse) throws Exception;

	/**
	 * 判断是否支持pjax，并且从原始模板名获取新的模板名称
	 * 
	 * @since 6.4
	 * @param templateName
	 * @param container
	 *            X-PJAX-Container value <b> 可能为null </b>
	 * @return
	 */
	default String processPjaxTemplateName(String templateName, HttpServletRequest request) throws LogicException {
		throw new LogicException("templateRender.pjax.unsupport", "不支持pjax的处理");
	}

}
