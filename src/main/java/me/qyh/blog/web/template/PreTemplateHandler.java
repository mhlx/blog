package me.qyh.blog.web.template;

import java.util.List;

import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.templateresource.ITemplateResource;

import me.qyh.blog.service.TemplateService.TemplateResource;

//must be public 
//can not be a inner class 
// handlerClass.getConstructor(new Class[0]);
public class PreTemplateHandler extends AbstractTemplateHandler {

	private static final int MAX_DEPTH = 10;

	public PreTemplateHandler() {
		super();
	}

	@Override
	public void setContext(ITemplateContext context) {
		ITemplateResource itr = context.getTemplateData().getTemplateResource();
		if (itr instanceof TemplateResource) {
			TemplateResource tr = (TemplateResource) itr;
			IEngineContext iec = (IEngineContext) context;
			iec.setVariable("this", tr.getTemplate());
		}
		List<TemplateData> templateStack = context.getTemplateStack();
		if (templateStack.size() > MAX_DEPTH) {
			throw new TemplateInputException("template max process depth is " + MAX_DEPTH);
		}
	}
}