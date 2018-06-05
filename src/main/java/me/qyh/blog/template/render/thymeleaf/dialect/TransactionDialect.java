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
