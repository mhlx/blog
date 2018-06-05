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

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.exception.LockException;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.util.Validators;

/**
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * 
 * @author mhlx
 *
 */
public class LockTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "lock";
	private static final int PRECEDENCE = 1000;
	private static final String ID = "id";
	private static final String TYPE = "type";

	public static final String VARIABLE_NAME = LockTagProcessor.class.getName();

	public LockTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
		this.lockManager = applicationContext.getBean(LockManager.class);
	}

	private final LockManager lockManager;

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		String lockId = tag.getAttributeValue(ID);
		if (Validators.isEmptyOrNull(lockId, true)) {
			structureHandler.removeElement();
			return;
		}
		boolean removed = false;
		try {
			String type = tag.getAttributeValue(TYPE);
			boolean block = "block".equalsIgnoreCase(type);

			LockException ex = null;
			Lock lock = null;
			try {
				lockManager.openLock(lockId);
			} catch (LockException e) {
				ex = e;
				lock = e.getLock();
			}

			if (ex == null) {
				if (block) {
					structureHandler.setLocalVariable(VARIABLE_NAME, new LockStructure());
					structureHandler.removeTags();
					removed = true;
				}
			} else {
				if (!block) {
					throw ex;
				}

				if (context.getVariable(VARIABLE_NAME) != null) {
					throw new TemplateProcessingException("lock标签中不能嵌套lock标签");
				}

				structureHandler.setLocalVariable(VARIABLE_NAME, new LockStructure(true, lock));
				structureHandler.removeTags();
				removed = true;
			}

		} finally {
			if (!removed) {
				structureHandler.removeElement();
			}
		}
	}

	public final class LockStructure {
		private final boolean locked;
		private final Lock lock;

		public LockStructure() {
			this(false, null);
		}

		public LockStructure(boolean locked, Lock lock) {
			super();
			this.locked = locked;
			this.lock = lock;
		}

		public boolean isLocked() {
			return locked;
		}

		public Lock getLock() {
			return lock;
		}

	}
}
