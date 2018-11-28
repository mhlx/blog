package me.qyh.blog.template.render.thymeleaf.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.context.Environment;

public class PrivateTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "private";
	private static final int PRECEDENCE = 1000;

	public PrivateTagProcessor(String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {
			Environment.doAuthencation();
		} finally {
			structureHandler.removeElement();
		}
	}

}
