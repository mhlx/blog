package me.qyh.blog.template.render.thymeleaf.dialect;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.template.render.ParseContextHolder;

/**
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * <p>
 * 开启一个<b>read-only</b>事务
 * </p>
 * 
 * @author mhlx
 *
 */
public class TransactionBeginTagProcessor extends TransactionSupport {

	private static final String TAG_NAME = "begin";
	private static final int PRECEDENCE = 1;
	private static final String ISOLATION_LEVEL = "isolationLevel";

	public TransactionBeginTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE, applicationContext);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		int isolationLevel = TransactionDefinition.ISOLATION_DEFAULT;
		String levelStr = tag.getAttributeValue(ISOLATION_LEVEL);
		if (levelStr != null) {
			try {
				isolationLevel = Integer.parseInt(levelStr);
			} catch (NumberFormatException e) {
			}
		}
		try {
			if (ParseContextHolder.getContext().getTransactionStatus() == null) {
				ParseContextHolder.getContext().setTransactionStatus(getTransactionStatus(isolationLevel));
			} else {
				throw new TemplateProcessingException("在开启一个事务前，应该先结束已经存在的事务");
			}
		} finally {
			structureHandler.removeElement();
		}
	}
}
