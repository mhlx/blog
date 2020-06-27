package me.qyh.blog.web.template;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.IEngineContextFactory;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.TemplateData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * blog use only ,only for web env
 *
 * @author wwwqyhme
 */
public class ThymeleafEngineContextFactory implements IEngineContextFactory {

    @Override
    public IEngineContext createEngineContext(IEngineConfiguration configuration, TemplateData templateData,
                                              Map<String, Object> templateResolutionAttributes, IContext context) {
        final Set<String> variableNames = context.getVariableNames();

        if (variableNames == null || variableNames.isEmpty()) {
            final IWebContext webContext = (IWebContext) context;
            return new ThymeleafWebEngineContext(configuration, templateData, templateResolutionAttributes,
                    webContext.getRequest(), webContext.getResponse(), webContext.getServletContext(),
                    webContext.getLocale(), Map.of());
        }

        final Map<String, Object> variables = new LinkedHashMap<String, Object>(variableNames.size() + 1, 1.0f);
        for (final String variableName : variableNames) {
            variables.put(variableName, context.getVariable(variableName));
        }

        final IWebContext webContext = (IWebContext) context;
        return new ThymeleafWebEngineContext(configuration, templateData, templateResolutionAttributes,
                webContext.getRequest(), webContext.getResponse(), webContext.getServletContext(),
                webContext.getLocale(), variables);
    }

}
