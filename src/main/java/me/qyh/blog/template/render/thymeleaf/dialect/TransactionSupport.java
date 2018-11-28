package me.qyh.blog.template.render.thymeleaf.dialect;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

public abstract class TransactionSupport extends AbstractElementTagProcessor {

	private final PlatformTransactionManager transactionManager;

	public TransactionSupport(TemplateMode templateMode, String dialectPrefix, String elementName,
			boolean prefixElementName, String attributeName, boolean prefixAttributeName, int precedence,
			ApplicationContext applicationContext) {
		super(templateMode, dialectPrefix, elementName, prefixElementName, attributeName, prefixAttributeName,
				precedence);
		this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
	}

	protected PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	protected TransactionStatus getTransactionStatus(int isolationLevel) {
		DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
		defaultTransactionDefinition.setReadOnly(true);
		defaultTransactionDefinition.setIsolationLevel(isolationLevel);
		return transactionManager.getTransaction(defaultTransactionDefinition);
	}

}
