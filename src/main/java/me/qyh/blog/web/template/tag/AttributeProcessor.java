package me.qyh.blog.web.template.tag;

import me.qyh.blog.web.template.TemplateUtils;
import org.attoparser.util.TextUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.processor.StandardDefaultAttributesTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Map;

class AttributeProcessor {

    private AttributeProcessor() {
        super();
    }

    static Map<String, String> process(TemplateMode templateMode, IProcessableElementTag tag, ITemplateContext context,
                                       String prefix) {
        final IAttribute[] attributes = tag.getAllAttributes();

        // Should be no problem in performing modifications during iteration, as the
        // attributeNames list
        // should not be affected by modifications on the original tag attribute set
        Map<String, String> attributeMap = new HashMap<>();
        for (final IAttribute attribute : attributes) {

            final AttributeName attributeName = attribute.getAttributeDefinition().getAttributeName();
            if (attributeName.isPrefixed()) {
                // always case sensitive
                if (TextUtil.equals(true, attributeName.getPrefix(), prefix)) {

                    // We will process each 'default' attribute separately
                    processDefaultAttribute(templateMode, context, tag, attribute, attributeMap);
                    continue;
                }
            }
            attributeMap.put(attribute.getAttributeCompleteName(), attribute.getValue());
        }
        return attributeMap;
    }

    // this method is copied from
    // StandardDefaultAttributesTagProcessor#processDefaultAttribute
    // if there's a better way to do this,pls let me know
    private static void processDefaultAttribute(final TemplateMode templateMode, final ITemplateContext context,
                                                final IProcessableElementTag tag, final IAttribute attribute, Map<String, String> attributeMap) {
        try {

            final AttributeName attributeName = attribute.getAttributeDefinition().getAttributeName();
            final String attributeValue = attribute.getValue();

            /*
             * Compute the new attribute name (i.e. the same, without the prefix)
             */
            final String originalCompleteAttributeName = attribute.getAttributeCompleteName();
            final String canonicalAttributeName = attributeName.getAttributeName();

            final String newAttributeName;
            if (TextUtil.endsWith(true, originalCompleteAttributeName, canonicalAttributeName)) {
                newAttributeName = canonicalAttributeName; // We avoid creating a new String instance
            } else {
                newAttributeName = originalCompleteAttributeName
                        .substring(originalCompleteAttributeName.length() - canonicalAttributeName.length());
            }

            final String newAttributeValue = TemplateUtils.processExpression(attributeValue, context);
            if (newAttributeValue != null) {
                attributeMap.put(newAttributeName, newAttributeValue);
            }

        } catch (final TemplateProcessingException e) {
            // This is a nice moment to check whether the execution raised an error and, if
            // so, add location information
            // Note this is similar to what is done at the superclass
            // AbstractElementTagProcessor, but we can be more
            // specific because we know exactly what attribute was being executed and caused
            // the error
            if (!e.hasTemplateName()) {
                e.setTemplateName(tag.getTemplateName());
            }
            if (!e.hasLineAndCol()) {
                e.setLineAndCol(attribute.getLine(), attribute.getCol());
            }
            throw e;
        } catch (final Exception e) {
            throw new TemplateProcessingException("Error during execution of processor '"
                    + StandardDefaultAttributesTagProcessor.class.getName() + "'", tag.getTemplateName(),
                    attribute.getLine(), attribute.getCol(), e);
        }
    }

}
