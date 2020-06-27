package me.qyh.blog.web.template;

import me.qyh.blog.entity.Template;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.*;
import org.thymeleaf.templateresource.ITemplateResource;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class TemplateUtils {

    private TemplateUtils() {
        super();
    }

    private static final String PREVIEW_REQUEST_KEY = TemplateUtils.class.getName() + ".PREVIEW_REQUEST";

    public static String processExpression(String expressionStr, ITemplateContext context) {
        if (expressionStr == null) {
            return null;
        }
        final IStandardExpressionParser expressionParser = StandardExpressions
                .getExpressionParser(context.getConfiguration());
        final Object expressionResult;
        final IStandardExpression expression = expressionParser.parseExpression(context, expressionStr);
        if (expression != null && expression instanceof FragmentExpression) {
            throw new TemplateProcessingException("expression can not be a FragmentExpression");
        }
        expressionResult = expression.execute(context, StandardExpressionExecutionContext.NORMAL);
        if (expressionResult == null) {
            return null;
        }
        if (expressionResult == NoOpToken.VALUE) {
            return null;
        }
        return expressionResult.toString();
    }

    public static void setPreviewState(HttpServletRequest request, boolean preview) {
        if (request.getAttribute(PREVIEW_REQUEST_KEY) == null)
            request.setAttribute(PREVIEW_REQUEST_KEY, preview);
    }

    public static boolean isPreviewRequest(HttpServletRequest request) {
        Object previewAttributeValue = request.getAttribute(PREVIEW_REQUEST_KEY);
        return previewAttributeValue != null && (Boolean) previewAttributeValue;
    }

    public static Optional<Integer> getTemplateId(ITemplateContext context) {
        Object _this = context.getVariable("this");
        if (_this != null && _this instanceof Template) {
            return Optional.of(((Template) _this).getId());
        }
        ITemplateResource resource = context.getTemplateData().getTemplateResource();
        if (resource instanceof TemplateResolver.TemplateResource) {
            return Optional.of(((TemplateResolver.TemplateResource) resource).getTemplate().getId());
        }
        return Optional.empty();
    }
}
