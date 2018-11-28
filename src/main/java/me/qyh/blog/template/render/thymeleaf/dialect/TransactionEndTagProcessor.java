package me.qyh.blog.template.render.thymeleaf.dialect;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.template.render.ParseContextHolder;

/**
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * 
 * <p>
 * 提交一个事务
 * </p>
 * 
 * @author mhlx
 *
 */
public class TransactionEndTagProcessor extends TransactionSupport {

	private static final String TAG_NAME = "end";
	private static final int PRECEDENCE = 1;

	public TransactionEndTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE, applicationContext);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {
			TransactionStatus status = ParseContextHolder.getContext().getTransactionStatus();
			if (status != null) {
				getTransactionManager().commit(status);
				ParseContextHolder.getContext().setTransactionStatus(null);
			}
		} finally {
			structureHandler.removeElement();
		}
	}
}
