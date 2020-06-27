package me.qyh.blog.web.template.tag;

import me.qyh.blog.web.template.TemplateProcessingWrapException;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RedirectTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "redirect";
    private static final String URL_ATTR = "url";
    // 是否是301跳转
    private static final String MOVED_PERMANENTLY_ATTR = "permanently";

    public RedirectTagProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, 1000);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                             IElementTagStructureHandler structureHandler) {
        structureHandler.removeElement();
        Map<String, String> map = AttributeProcessor.process(getTemplateMode(), tag, context, "th");
        String url = map.get(URL_ATTR);
        if (url == null) {
            return;
        }
        URL _url;
        try {
            _url = new URL(url);
        } catch (MalformedURLException e) {
            return;
        }
        RedirectException ex = new RedirectException(_url.toString(),
                Boolean.parseBoolean(map.get(MOVED_PERMANENTLY_ATTR)));
        throw TemplateProcessingWrapException.wrap(ex);
    }

}
