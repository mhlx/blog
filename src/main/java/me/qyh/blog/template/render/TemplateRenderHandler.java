package me.qyh.blog.template.render;

import javax.servlet.http.HttpServletRequest;

/**
 * @see MatchableTemplateRenderHandler
 * @see NamedTemplateRenderHandler
 * @author wwwqyhme
 *
 */
public interface TemplateRenderHandler {

	boolean match(String templateName, HttpServletRequest request, String contentType);

	String afterRender(String content, HttpServletRequest request, String contentType);

}
