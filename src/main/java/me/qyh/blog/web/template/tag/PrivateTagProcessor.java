package me.qyh.blog.web.template.tag;

import me.qyh.blog.BlogContext;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.web.template.TemplateProcessingWrapException;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

public class PrivateTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "private";

    public PrivateTagProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                             IElementTagStructureHandler structureHandler) {
        try {
            if (!BlogContext.isAuthenticated()) {
                throw new TemplateProcessingWrapException(new AuthenticationException());
            }
        } finally {
            structureHandler.removeElement();
        }
    }

}
