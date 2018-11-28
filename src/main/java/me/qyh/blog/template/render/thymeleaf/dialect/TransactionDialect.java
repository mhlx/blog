package me.qyh.blog.template.render.thymeleaf.dialect;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

public class TransactionDialect extends AbstractProcessorDialect {

	private final ApplicationContext applicationContext;

	public TransactionDialect(ApplicationContext applicationContext) {
		super("Transaction Dialect", "transaction", 1);
		this.applicationContext = applicationContext;
	}

	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {
		return Set.of(new TransactionBeginTagProcessor(dialectPrefix, applicationContext),
				new TransactionEndTagProcessor(dialectPrefix, applicationContext));
	}

}
