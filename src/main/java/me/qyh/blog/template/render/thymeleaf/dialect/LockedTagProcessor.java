package me.qyh.blog.template.render.thymeleaf.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.template.render.thymeleaf.dialect.LockTagProcessor.LockStructure;

/**
 * 
 *
 */
public class LockedTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "locked";
	private static final int PRECEDENCE = 1000;

	public LockedTagProcessor(String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		LockStructure structure = (LockStructure) context.getVariable(LockTagProcessor.VARIABLE_NAME);
		if (structure == null) {
			throw new TemplateProcessingException("locked标签必须为lock标签的子标签");
		}
		if (structure.isLocked()) {
			Lock lock = structure.getLock();
			structureHandler.setLocalVariable("lock", lock);
			structureHandler.removeTags();
		} else {
			structureHandler.removeElement();
		}
	}
}
