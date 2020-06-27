package me.qyh.blog.web.template.expression;

import me.qyh.blog.Markdown2Html;
import me.qyh.blog.service.TemplateService;
import org.springframework.context.MessageSource;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Set;

public class BlogExpressionObjectFactory implements IExpressionObjectFactory {

    private static final String TIMES_EXPRESSION_OBJECT_NAME = "times";
    private static final String MESSAGER_EXPRESSION_OBJECT_NAME = "messager";
    private static final String JSOUP_EXPRESSION_OBJECT_NAME = "jsoups";
    private static final String PAGING_EXPRESSION_OBJECT_NAME = "pagings";
    private static final String URL_EXPRESSION_OBJECT_NAME = "urls";
    private static final String FORMATS_EXPRESSION_OBJECT_NAME = "formats";
    private static final String M2H_EXPRESSION_OBJECT_NAME = "m2h";
    private static final String AUTH_EXPRESSION_OBJECT_NAME = "auth";
    private static final String HELPER_EXPRESSION_OBJECT_NAME = "helper";

    private final MessageSource messageSource;
    private final Markdown2Html markdown2Html;
    private final TemplateService templateService;

    public BlogExpressionObjectFactory(TemplateService templateService,MessageSource messageSource, Markdown2Html markdown2Html) {
        super();
        this.templateService = templateService;
        this.messageSource = messageSource;
        this.markdown2Html = markdown2Html;
    }

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return Set.of(TIMES_EXPRESSION_OBJECT_NAME, MESSAGER_EXPRESSION_OBJECT_NAME, JSOUP_EXPRESSION_OBJECT_NAME,
                PAGING_EXPRESSION_OBJECT_NAME, URL_EXPRESSION_OBJECT_NAME, FORMATS_EXPRESSION_OBJECT_NAME,
                M2H_EXPRESSION_OBJECT_NAME, AUTH_EXPRESSION_OBJECT_NAME,HELPER_EXPRESSION_OBJECT_NAME);
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (TIMES_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Times();
        }
        if (MESSAGER_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Messager(messageSource);
        }
        if (JSOUP_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Jsoups();
        }
        if (PAGING_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Pagings();
        }
        if (URL_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            WebEngineContext wec = (WebEngineContext) context;
            return new Urls(wec.getRequest(), context);
        }
        if (FORMATS_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Formats();
        }
        if (M2H_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new M2h(markdown2Html);
        }
        if (AUTH_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new Auth();
        }
        if (HELPER_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return templateService.getTemplateHelper();
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        if (URL_EXPRESSION_OBJECT_NAME.equals(expressionObjectName) || HELPER_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return false;
        }
        return true;
    }

}
