package me.qyh.blog.plugin.comment.thymeleaf.dialect;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentService;
import me.qyh.blog.template.render.thymeleaf.dialect.DefaultAttributesTagProcessor;

/**
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * 
 * @author mhlx
 *
 */
public class CommentTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "comment";
	private static final int PRECEDENCE = 1000;
	private static final String ID = "id";
	private static final String MODULE = "module";

	public static final String VARIABLE_NAME = CommentTagProcessor.class.getName();

	public CommentTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
		this.commentService = applicationContext.getBean(CommentService.class);
	}

	private final CommentService commentService;

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		CommentModule commentModule = null;
		Map<String, String> map = processAttribute(context, tag);
		String module = map.get(MODULE);
		if (module != null) {
			commentModule = new CommentModule();
			commentModule.setModule(module);

			String idStr = map.get(ID);
			if (idStr != null) {
				try {
					Integer id = Integer.parseInt(idStr);
					commentModule.setId(id);
				} catch (NumberFormatException ex) {
					// ignore
				}
			}
		}

		boolean hasCommented = commentService.hasCommented(commentModule);
		structureHandler.setLocalVariable(VARIABLE_NAME, hasCommented);
		structureHandler.removeTags();
	}
}
