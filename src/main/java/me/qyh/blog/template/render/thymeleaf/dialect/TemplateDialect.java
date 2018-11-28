package me.qyh.blog.template.render.thymeleaf.dialect;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

public class TemplateDialect extends AbstractProcessorDialect {

	private static final String DIALECT_NAME = "Template Dialect";

	private final ApplicationContext applicationContext;

	public TemplateDialect(ApplicationContext applicationContext) {
		super(DIALECT_NAME, "template", StandardDialect.PROCESSOR_PRECEDENCE);
		this.applicationContext = applicationContext;
	}

	@Override
	public Set<IProcessor> getProcessors(final String dialectPrefix) {
		return Set.of(new DataTagProcessor(dialectPrefix, applicationContext),
				new FragmentTagProcessor(dialectPrefix, applicationContext),
				new LockTagProcessor(dialectPrefix, applicationContext),
				new RedirectTagProcessor(dialectPrefix, applicationContext), new PrivateTagProcessor(dialectPrefix),
				new PeriodTagProcessor(dialectPrefix), new LockedTagProcessor(dialectPrefix),
				new UnlockedTagProcessor(dialectPrefix), new MarkdownModelProcessor(dialectPrefix, applicationContext),
				new HandlerTagProcessor(dialectPrefix));
	}

}
