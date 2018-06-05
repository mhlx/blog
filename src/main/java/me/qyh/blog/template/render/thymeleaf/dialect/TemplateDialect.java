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
				new UnlockedTagProcessor(dialectPrefix), new MarkdownModelProcessor(dialectPrefix, applicationContext));
	}

}