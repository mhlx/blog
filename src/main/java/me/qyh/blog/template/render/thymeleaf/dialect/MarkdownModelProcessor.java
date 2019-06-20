package me.qyh.blog.template.render.thymeleaf.dialect;

import java.io.IOException;
import java.io.Writer;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.UrlResource;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeNames;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.processor.element.MatchingAttributeName;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EscapedAttributeUtils;
import org.thymeleaf.util.FastStringWriter;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.util.Validators;

/**
 * 能够使用 markdown标签
 * <p>
 * <b>markdown标签内的标签将不会被解析！！，如果需要获取数据，需要采用内联表达式</b>
 * </p>
 * 
 * @since 5.9
 * @author wwwqyhme
 *
 */
public class MarkdownModelProcessor extends AbstractElementModelProcessor {

	private static final int PRECEDENCE = 1000;
	// @since 7.1.2
	private static final String SRC_ATTR = "src";

	private Markdown2Html markdown2Html;
	private UrlHelper urlHelper;

	private MatchingAttributeName man;

	public MarkdownModelProcessor(String dialectPrefix, ApplicationContext ctx) {
		super(TemplateMode.HTML, dialectPrefix, "markdown", false, null, false, PRECEDENCE);
		this.markdown2Html = ctx.getBean(Markdown2Html.class);
		this.urlHelper = ctx.getBean(UrlHelper.class);
		// th:src can not be parsed???
		this.man = MatchingAttributeName.forAttributeName(TemplateMode.HTML,
				AttributeNames.forName(TemplateMode.HTML, null, SRC_ATTR));
	}

	@Override
	protected void doProcess(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
		int size = model.size();
		if (size <= 2) {
			model.reset();
			// try to get src attribute value
			IProcessableElementTag firstEvent = (IProcessableElementTag) model.get(0);
			String attributeValue = EscapedAttributeUtils.unescapeAttribute(context.getTemplateMode(),
					firstEvent.getAttributeValue(man.getMatchingAttributeName()));

			if (!Validators.isEmptyOrNull(attributeValue, true)) {
				String md = getStringByUrl(attributeValue);
				model.add(context.getModelFactory().createText(markdown2Html.toHtml(md)));
			}

			return;
		}
		boolean reset = false;
		try {
			if (removeOpenTag(model)) {
				try (Writer writer = new FastStringWriter()) {
					model.write(writer);
					model.reset();
					reset = true;

					model.add(context.getModelFactory().createText(markdown2Html.toHtml(writer.toString())));

				} catch (IOException e) {
					throw new TemplateProcessingException(e.getMessage(), e);
				}
			}
		} finally {
			if (!reset)
				model.reset();
		}
	}

	protected boolean removeOpenTag(IModel model) {
		int size = model.size();
		ITemplateEvent last = model.get(size - 1);
		if (last instanceof ICloseElementTag) {
			model.remove(size - 1);
		} else {
			return false;
		}
		model.remove(0);// remove <markdown>
		return true;
	}

	protected String getStringByUrl(String url) {
		try {
			return Resources.readResourceToString(
					new UrlResource(UrlUtils.isAbsoluteUrl(url) ? url : urlHelper.getUrls().getUrl(url)));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
