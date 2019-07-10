package me.qyh.blog.plugin.staticfile;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.plugin.staticfile.vo.FileContent;
import me.qyh.blog.template.TemplateRequestMappingHandlerMapping;

/**
 * @since 7.1.3
 * @author wwwqyhme
 *
 */
public class RenderStaticFileController implements InitializingBean {

	@Autowired
	private StaticFileManager handler;
	@Autowired
	private Markdown2Html m2h;
	@Autowired
	private TemplateRequestMappingHandlerMapping mapping;
	private final String urlPrefix;
	private final String templateName;

	RenderStaticFileController(String urlPrefix, String templateName) {
		super();
		this.urlPrefix = urlPrefix;
		this.templateName = templateName;
	}

	public String renderMd(HttpServletRequest request, Model model) throws LogicException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (path == null) {
			throw new SystemException(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "属性为null");
		}
		FileContent fc;
		try {
			int add = urlPrefix.isEmpty() ? 1 : 2;
			fc = handler.getEditableFile(path.substring(urlPrefix.length() + add));
		} catch (LogicException e) {
			if ("staticFile.notExists".equals(e.getLogicMessage().getCodes()[0])) {
				throw new ResourceNotFoundException(e.getLogicMessage());
			}
			throw e;
		}
		model.addAttribute("content", m2h.toHtml(fc.getContent()));
		return templateName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mapping.registerMapping(RequestMappingInfo.paths(urlPrefix + "/**/*.md").methods(RequestMethod.GET),
				this.getClass().getName(),
				RenderStaticFileController.class.getMethod("renderMd", HttpServletRequest.class, Model.class));
	}
}
