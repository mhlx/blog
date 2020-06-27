package me.qyh.blog.web.template.tag;

import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.security.PasswordProtect;
import me.qyh.blog.security.SecurityChecker;
import me.qyh.blog.web.template.TemplateProcessingWrapException;
import me.qyh.blog.web.template.TemplateUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Optional;

public class PasswordTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "password";
    private static final String VALUE_ATTR = "value";

    public PasswordTagProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                             IElementTagStructureHandler structureHandler) {
        try {
            Optional<Integer> opId = TemplateUtils.getTemplateId(context);
            if (opId.isEmpty()) {
                return;
            }
            final String value = tag.getAttributeValue(VALUE_ATTR);
            if (value == null) {
                return;
            }
            PasswordProtect resource = new PasswordProtect() {

                @Override
                public String getPassword() {
                    return value;
                }

                @Override
                public String getResId() {
                    return "template-" + opId.get();
                }
            };
            try {
                SecurityChecker.check(resource);
            } catch (AuthenticationException e) {
                throw new TemplateProcessingWrapException(e);
            }
        } finally {
            structureHandler.removeElement();
        }
    }

}
