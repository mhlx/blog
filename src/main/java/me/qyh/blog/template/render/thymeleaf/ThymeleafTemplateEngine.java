/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.template.render.thymeleaf;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.thymeleaf.dialect.IPreProcessorDialect;
import org.thymeleaf.preprocessor.IPreProcessor;
import org.thymeleaf.preprocessor.PreProcessor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

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