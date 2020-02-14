package me.qyh.blog.web.template.tag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.web.template.TemplateProcessingWrapException;
import me.qyh.blog.web.template.ProcessContext;

public class DataTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "data";
	private static final String NAME_ATTR = "name";
	private static final String ALIAS_ATTR = "alias";

	private Map<String, DataProvider<?>> providers = new ConcurrentHashMap<>();

	private final PlatformTransactionManager platformTransactionManager;

	public DataTagProcessor(String dialectPrefix, PlatformTransactionManager platformTransactionManager) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
		this.platformTransactionManager = platformTransactionManager;
	}

	public PlatformTransactionManager getPlatformTransactionManager() {
		return platformTransactionManager;
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {
			Map<String, String> map = AttributeProcessor.process(getTemplateMode(), tag, context, "th");
			String name = map.remove(NAME_ATTR);
			if (name == null) {
				return;
			}

			DataProvider<?> provider = providers.get(name);
			if (provider == null) {
				return;
			}

			String alias = map.remove(ALIAS_ATTR);
			if (alias == null) {
				alias = name;
			}

			Object object;
			try {
				// open a readonly transaction if there's no transaction
				if (provider.shouldExecuteInTransaction()
						&& !TransactionSynchronizationManager.isActualTransactionActive()) {
					DefaultTransactionDefinition td = new DefaultTransactionDefinition();
					td.setReadOnly(true);
					TransactionStatus ts = platformTransactionManager.getTransaction(td);
					ProcessContext.setTransactionStatus(ts);
				}
				object = provider.provide(map);
			} catch (Exception e) {
				throw TemplateProcessingWrapException.wrap(e);
			}

			// just to check reserved word
			IEngineContext engineContext = (IEngineContext) context;
			engineContext.setVariable(alias, object);
			engineContext.removeVariable(alias);

			// put data in request attribute
			// if we put it via EngineContext ,it will auto removed after tag was processed
			IWebContext webContext = (IWebContext) context;
			webContext.getRequest().setAttribute(alias, object);
		} finally {
			structureHandler.removeElement();
		}
	}

	public void registerIfAbsent(DataProvider<?> provider) {
		providers.putIfAbsent(provider.getName(), provider);
	}

	public void registerDataProvider(DataProvider<?> provider) {
		providers.compute(provider.getName(), (k, v) -> {
			if (v != null) {
				throw new RuntimeException("已经存在名称为:" + k + "的DataProvider了");
			}
			return provider;
		});
	}
}
