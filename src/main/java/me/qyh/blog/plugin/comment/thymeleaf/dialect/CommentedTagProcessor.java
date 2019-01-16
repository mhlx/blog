package me.qyh.blog.plugin.comment.thymeleaf.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * 
 *
 */
public class CommentedTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "commented";
	private static final int PRECEDENCE = 1000;

	public CommentedTagProcessor(String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		Boolean hasCommented = (Boolean) context.getVariable(CommentTagProcessor.VARIABLE_NAME);
		if (hasCommented == null) {
			throw new TemplateProcessingException("commented标签必须为comment标签的子标签");
		}
		if (!hasCommented) {
			structureHandler.removeElement();
		} else {
			structureHandler.removeTags();
		}
	}
}
