package me.qyh.blog.template.render.thymeleaf.dialect;

import java.util.Map;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.render.ParseContextHolder;

public class HandlerTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "handler";
	private static final int PRECEDENCE = 1000;
	private static final String NAME_ATTR = "name";

	public HandlerTagProcessor(String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {
			Map<String, String> attMap = processAttribute(context, tag);
			String name = attMap.get(NAME_ATTR);
			if (Validators.isEmptyOrNull(name, true)) {
				return;
			}
			ParseContextHolder.getContext().getNamedRenderHandlers().put(name, attMap);
		} finally {
			structureHandler.removeElement();
		}
	}

}
