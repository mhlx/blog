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
import me.qyh.blog.core.vo.UnlockResult;

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

			/**
			 * @since 6.6 不应该吃掉
			 *        lockManager解锁抛出的异常，如果吃掉，lockManager回滚，此时如果在大事务内(transaction:end标签)，大事务仍然提交，会产生异常
			 */
			UnlockResult result = lockManager.openLockQuietly(lockId);

			if (result.isUnlocked()) {
				if (block) {
					structureHandler.setLocalVariable(VARIABLE_NAME, new LockStructure());
					structureHandler.removeTags();
					removed = true;
				}
			} else {
				if (!block) {
					throw new LockException(result.getLock(), result.getError());
				}

				if (context.getVariable(VARIABLE_NAME) != null) {
					throw new TemplateProcessingException("lock标签中不能嵌套lock标签");
				}

				structureHandler.setLocalVariable(VARIABLE_NAME, new LockStructure(true, result.getLock()));
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
