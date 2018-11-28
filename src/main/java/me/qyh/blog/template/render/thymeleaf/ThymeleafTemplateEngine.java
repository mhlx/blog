package me.qyh.blog.template.render.thymeleaf;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dialect.IPreProcessorDialect;
import org.thymeleaf.preprocessor.IPreProcessor;
import org.thymeleaf.preprocessor.PreProcessor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.template.render.thymeleaf.dialect.GsonStandardJavaScriptSerializer;
import me.qyh.blog.template.render.thymeleaf.dialect.PreTemplateHandler;
import me.qyh.blog.template.render.thymeleaf.dialect.TemplateDialect;
import me.qyh.blog.template.render.thymeleaf.dialect.TransactionDialect;

public class ThymeleafTemplateEngine extends SpringTemplateEngine
		implements ApplicationListener<ContextRefreshedEvent> {

	public ThymeleafTemplateEngine() {
		super();
		addDialect(new IPreProcessorDialect() {

			@Override
			public String getName() {
				return "Blog Template Engine PreProcessor Dialect";
			}

			@Override
			public Set<IPreProcessor> getPreProcessors() {
				return Set.of(new PreProcessor(TemplateMode.HTML, PreTemplateHandler.class, 1000));
			}

			@Override
			public int getDialectPreProcessorPrecedence() {
				return 1000;
			}
		});
		// @since 6.5
		final Set<IDialect> dialects = getDialects();
		for (final IDialect dialect : dialects) {
			if (dialect instanceof SpringStandardDialect) {
				((SpringStandardDialect) dialect).setJavaScriptSerializer(new GsonStandardJavaScriptSerializer());
			}
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			return;
		}
		ApplicationContext applicationContext = event.getApplicationContext();
		addDialect(new TemplateDialect(applicationContext));
		addDialect(new TransactionDialect(applicationContext));
	}

}
