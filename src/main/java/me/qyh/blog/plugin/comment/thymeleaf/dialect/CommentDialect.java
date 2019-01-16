package me.qyh.blog.plugin.comment.thymeleaf.dialect;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

public class CommentDialect extends AbstractProcessorDialect {

	private static final String DIALECT_NAME = "Comment Dialect";

	private final ApplicationContext applicationContext;

	public CommentDialect(ApplicationContext applicationContext) {
		super(DIALECT_NAME, "template", StandardDialect.PROCESSOR_PRECEDENCE);
		this.applicationContext = applicationContext;
	}

	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {
		return Set.of(new CommentTagProcessor(dialectPrefix, applicationContext),
				new CommentedTagProcessor(dialectPrefix), new UnCommentedTagProcessor(dialectPrefix));
	}
}
