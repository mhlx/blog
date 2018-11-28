package me.qyh.blog.template.render.thymeleaf.dialect;

import java.util.List;

import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.templateresource.ITemplateResource;

import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.template.PreviewTemplate;
import me.qyh.blog.template.Template;
import me.qyh.blog.template.render.ParseContext;
import me.qyh.blog.template.render.ParseContextHolder;
import me.qyh.blog.template.render.ParsedTemplate;
import me.qyh.blog.template.render.thymeleaf.ThymeleafTemplateResolver.TemplateResource;

/**
 * 
 * @author mhlx
 *
 */
public final class PreTemplateHandler extends AbstractTemplateHandler {

	private static final int MAX_DEPTH = 10;

	public PreTemplateHandler() {
		super();
	}

	@Override
	public void setContext(ITemplateContext context) {
		ParseContext parseContext = ParseContextHolder.getContext();
		TemplateData templateData = context.getTemplateData();
		List<TemplateData> templateStack = context.getTemplateStack();

		ITemplateResource templateResource = templateData.getTemplateResource();
		if (templateResource instanceof TemplateResource) {
			Template template = ((TemplateResource) templateResource).getTemplate();
			addTemplateDataStack(PreviewTemplate.isPreviewTemplate(template.getTemplateName()), template.isCallable(),
					templateStack);
			if (!parseContext.getRoot().map(ParsedTemplate::isCallable).orElse(false)
					&& parseContext.isOnlyCallable()) {
				throw new RuntimeLogicException(new Message("template.notCallable", "模板无法被调用"));
			}
			// TemplateResource 可能来自于缓存，为了防止修改数据，这里clone后传给页面
			if (template instanceof PreviewTemplate) {
				((IEngineContext) context).setVariable("this",
						((PreviewTemplate) template).getOriginalTemplate().cloneTemplate());
			} else {
				((IEngineContext) context).setVariable("this", template.cloneTemplate());
			}
		} else {
			addTemplateDataStack(false, false, templateStack);
		}

		if (templateStack.size() > MAX_DEPTH) {
			throw new TemplateProcessingException("已经达到模板处理的最大深度");
		}

		super.setContext(context);
	}

	private void addTemplateDataStack(boolean preview, boolean callable, List<TemplateData> datas) {
		ParseContext context = ParseContextHolder.getContext();
		ParsedTemplate root = context.getRoot().orElse(null);
		if (root == null) {
			context.setRoot(toParsedTemplate(datas.get(0), preview, callable));
		} else {
			addChain(root, preview, callable, datas);
		}
	}

	private void addChain(ParsedTemplate root, boolean preview, boolean callable, List<TemplateData> datas) {
		int length = datas.size();
		if (length == 1) {
			return;
		}
		if (length == 2) {
			root.addChild(toParsedTemplate(datas.get(1), preview, callable));
			return;
		}
		ParsedTemplate child = root;
		for (int i = 1; i < length - 1; i++) {
			TemplateData data = datas.get(i);
			child = child.getChild(data.getTemplate()).orElseThrow();
		}
		child.addChild(toParsedTemplate(datas.get(length - 1), preview, callable));
	}

	private ParsedTemplate toParsedTemplate(TemplateData data, boolean preview, boolean callable) {
		return new ParsedTemplate(data.getTemplate(), preview, callable);
	}
}
