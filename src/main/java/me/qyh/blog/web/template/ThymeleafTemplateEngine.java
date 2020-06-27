package me.qyh.blog.web.template;

import org.springframework.util.ReflectionUtils;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ThymeleafTemplateEngine extends SpringTemplateEngine {

    private static final String[] RESTRICT_FIELD_NAMES = new String[]{"restrictVariableAccess", "forbidUnsafeExpressionResults", "performTypeConversion"};

    public void setRestrict(boolean restrict) {
        if (!restrict) {
            Arrays.stream(RESTRICT_FIELD_NAMES).forEach(this::setRestrictField);
        }
    }

    private void setRestrictField(String fieldName) {
        Field field = ReflectionUtils.findField(StandardExpressionExecutionContext.class, fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, StandardExpressionExecutionContext.RESTRICTED, false);
        ReflectionUtils.setField(field, StandardExpressionExecutionContext.RESTRICTED_FORBID_UNSAFE_EXP_RESULTS, false);
    }
}
