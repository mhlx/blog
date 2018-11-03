package me.qyh.blog.template.render;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 7.0
 * @author wwwqyhme
 *
 */
public interface NamedTemplateRenderHandler {

	/**
	 * 处理器名称
	 * 
	 * @return
	 */
	String name();

	/**
	 * 
	 * @param content
	 *            页面解析后的内容
	 * @param request
	 *            请求
	 * @param contentType
	 *            页面响应类型
	 * @param attrs
	 *            属性 <b>不为null</b>
	 * @return
	 */
	String afterRender(String content, HttpServletRequest request, String contentType, Map<String, String> attrs);
}
