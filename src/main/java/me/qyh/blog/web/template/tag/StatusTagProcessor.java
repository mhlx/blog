package me.qyh.blog.web.template.tag;

import me.qyh.blog.Message;
import me.qyh.blog.utils.StringUtils;
import me.qyh.blog.web.template.TemplateProcessingWrapException;
import org.springframework.http.HttpStatus;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Map;

public class StatusTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "status";
    private static final String CODE_ATTR = "code";
    private static final String MSG_CODE_ATTR = "msgCode";
    private static final String DEFAULT_MESSAGE_ATTR = "defaultMsg";

    public StatusTagProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                             IElementTagStructureHandler structureHandler) {
        Map<String, String> map = AttributeProcessor.process(getTemplateMode(), tag, context, "th");
        HttpStatus status = HttpStatus.valueOf(Integer.parseInt(map.get(CODE_ATTR)));
        if (!status.isError()) {
            throw new TemplateProcessingException("status tag only support 4xx or 5xx code");
        }
        Message message = null;
        String msgCode = map.get(MSG_CODE_ATTR);
        if (!StringUtils.isNullOrBlank(msgCode)) {
            String defaultMessage = map.get(DEFAULT_MESSAGE_ATTR);
            message = new Message(msgCode, defaultMessage);
        }
        throw TemplateProcessingWrapException.wrap(new HttpStatusException(status, message));
    }

}
