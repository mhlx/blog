package me.qyh.blog.web.template.tag;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.web.template.ProcessContext;
import me.qyh.blog.web.template.TemplateDataRequest;
import me.qyh.blog.web.template.TemplateProcessingWrapException;
import me.qyh.blog.web.template.TemplateRequestMappingHandlerAdapter;

public class DataTagProcessor extends AbstractElementTagProcessor {

	private static final String TAG_NAME = "data";
	private static final String PATH_ATTR = "path";
	private static final String ALIAS_ATTR = "alias";
	private static final String IGNORE_EXCEPTION_ATTR = "ignoreException";

	private static final TemplateRequestMappingHandlerAdapter adapter = TemplateRequestMappingHandlerAdapter
			.getRequestMappingHandlerAdapter();

	private final PlatformTransactionManager platformTransactionManager;

	public DataTagProcessor(String dialectPrefix, PlatformTransactionManager platformTransactionManager) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
		this.platformTransactionManager = platformTransactionManager;
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {
			Map<String, String> map = AttributeProcessor.process(getTemplateMode(), tag, context, "th");
			String path = map.remove(PATH_ATTR);
			if (path == null) {
				return;
			}

			String alias = map.remove(ALIAS_ATTR);
			if (alias == null) {
				return;
			}

			boolean ignoreException = "true".equalsIgnoreCase(map.remove(IGNORE_EXCEPTION_ATTR));

			Map<String, String[]> map2 = new HashMap<>();
			map.forEach((k, v) -> {
				if (k.startsWith("@")) {
					map2.put(k.substring(1), new String[] { v });
				} else {
					map2.put(k, new String[] { v });
				}
			});

			// put data in request attribute
			// if we put it via EngineContext ,it will auto removed after tag was processed
			IWebContext webContext = (IWebContext) context;
			HttpServletRequest request = webContext.getRequest();

			Object object = null;
			try {

				if (!TransactionSynchronizationManager.isActualTransactionActive()) {
					DefaultTransactionDefinition td = new DefaultTransactionDefinition();
					td.setReadOnly(true);
					TransactionStatus ts = platformTransactionManager.getTransaction(td);
					ProcessContext.setTransactionStatus(ts);
				}

				TemplateDataRequest req = ProcessContext.getTemplateDataRequest();

				if (req == null) {
					req = new TemplateDataRequest(request);
					ProcessContext.setTemplateDataRequest(req);
				}

				req.reset(map2, path);

				try {
					object = adapter.invoke(path, req);
				} catch (LogicException | AuthenticationException e) {
					if (!ignoreException) {
						throw e;
					}
				}
			} catch (Exception e) {
				throw TemplateProcessingWrapException.wrap(e);
			}

			// just to check reserved word
			IEngineContext engineContext = (IEngineContext) context;
			engineContext.setVariable(alias, object);
			engineContext.removeVariable(alias);
			request.setAttribute(alias, object);
		} finally {
			structureHandler.removeElement();
		}
	}
}
