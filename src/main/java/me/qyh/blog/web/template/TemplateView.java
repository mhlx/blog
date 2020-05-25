package me.qyh.blog.web.template;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.view.ThymeleafView;

import me.qyh.blog.web.BlogHandlerExceptionResolver;
import me.qyh.blog.web.template.tag.RedirectException;

public class TemplateView extends ThymeleafView {

	public TemplateView() {
		setProducePartialOutputWhileProcessing(false);
	}

	private static final Logger logger = LoggerFactory.getLogger(TemplateView.class);

	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	@Autowired
	private BlogHandlerExceptionResolver blogHandlerExceptionResolver;

	@Override
	protected void renderFragment(Set<String> markupSelectorsToRender, Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		try {
			super.renderFragment(markupSelectorsToRender, model, request, response);
		} catch (Throwable e) {
			rollbackAndCommit();// commit before further handle

			if (response.isCommitted()) {
				throw e;
			}

			String templateName = getTemplateName();
			if (StringUtils.startsWithIgnoreCase(templateName, "error/") || templateName.equalsIgnoreCase("unlock")) {
				// forward to error
				blogHandlerExceptionResolver.resolveErrorPageException(request, response, e);
				return;
			}

			if (templateName.equals("errorPageError")) {
				logger.error(e.getMessage(), e);
				throw e;
			}

			Throwable ex = unwrapException(e);
			if (ex == null && (e instanceof TemplateProcessingException)) {
				ex = e;
			}

			if (ex == null) {
				throw e;
			}

			if (ex instanceof RedirectException) {
				RedirectException re = (RedirectException) ex;
				if (re.isPermanently()) {
					response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
					response.setHeader("Location", re.getUrl());
				} else {
					response.sendRedirect(re.getUrl());
				}
				return;
			}
			// let's exception resolver handle it
			blogHandlerExceptionResolver.resolveTemplateException(request, response, model, (Exception) ex);
		} finally {
			ProcessContext.getTransactionStatus().ifPresent(platformTransactionManager::commit);
			ProcessContext.remove();
		}
	}

	private Throwable unwrapException(Throwable root) {
		if (root == null) {
			return null;
		}
		if (root instanceof TemplateProcessingWrapException) {
			return ((TemplateProcessingWrapException) root).getCause();
		}
		Throwable cause = root.getCause();
		if (cause == null) {
			return null;
		}
		return unwrapException(cause);
	}

	private void rollbackAndCommit() {
		ProcessContext.getTransactionStatus().ifPresent(status -> {
			status.setRollbackOnly();
			platformTransactionManager.commit(status);
		});
		ProcessContext.remove();
	}

}
