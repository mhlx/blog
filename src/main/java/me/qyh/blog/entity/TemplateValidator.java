package me.qyh.blog.entity;

import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class TemplateValidator implements Validator {

    private static final String NAME_REG = "^[a-zA-Z][a-zA-Z0-9*/_-]*$";
    private static final int CONTENT_MAX_LENGTH = 500000;
    private static final int NAME_MAX_LENGTH = 255;
    private static final int PATTERN_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final String PATTERN_REG = "^[A-Za-z0-9*/{}_-]+$";

    @Override
    public boolean supports(Class<?> clazz) {
        return Template.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Template template = (Template) target;
        String content = template.getContent();
        if (StringUtils.isNullOrBlank(content)) {
            errors.rejectValue("content", "NotBlank", "模板内容不能为空");
        } else if (content.length() > CONTENT_MAX_LENGTH) {
            errors.rejectValue("content", "Size", new Object[]{CONTENT_MAX_LENGTH},
                    "模板内容不能超过" + CONTENT_MAX_LENGTH + "个字符");
        }
        String pattern = template.getPattern();
        if (pattern != null) {
            pattern = "/" + FileUtils.cleanPath(pattern);
            if (pattern.length() > PATTERN_MAX_LENGTH) {
                errors.rejectValue("pattern", "Size", new Object[]{PATTERN_MAX_LENGTH},
                        "模板路径不能超过" + PATTERN_MAX_LENGTH + "个字符");
            } else if (!pattern.matches(PATTERN_REG)) {
                errors.rejectValue("pattern", "Pattern", "无效的路径，路径只能包含大小写英文字符、数字、以及_-{}/");
            }
            template.setPattern(pattern);
            template.setName(null);
        } else {
            String name = template.getName();
            if (StringUtils.isNullOrBlank(name)) {
                errors.rejectValue("name", "NotBlank", "当不设置路径时，名称不能为空");
            } else if (name.length() > NAME_MAX_LENGTH) {
                errors.rejectValue("name", "Size", new Object[]{NAME_MAX_LENGTH},
                        "模板名称不能超过" + NAME_MAX_LENGTH + "个字符");
            } else if (!name.matches(NAME_REG)) {
                errors.rejectValue("name", "Pattern", "无效的名称，名称只能包含大小写英文字符、数字、以及_-，并且必须要以英文字母开头");
            }
        }

        if (template.getEnable() == null) {
            errors.rejectValue("enable", "NotNull", "模板是否启用不能为空");
        }

        String description = template.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            errors.rejectValue("description", "Size", new Object[]{DESCRIPTION_MAX_LENGTH},
                    "模板描述长度不能超过" + DESCRIPTION_MAX_LENGTH + "个字符");
        }

//		if (template.getAllowComment() == null) {
//			errors.rejectValue("allowComment", "NotNull", "是否允许评论不能为空");
//		}
    }

}