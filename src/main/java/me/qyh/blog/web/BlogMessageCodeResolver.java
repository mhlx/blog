package me.qyh.blog.web;

import org.springframework.validation.MessageCodesResolver;

public class BlogMessageCodeResolver implements MessageCodesResolver {

    public static BlogMessageCodeResolver INSTANCE = new BlogMessageCodeResolver();

    private BlogMessageCodeResolver() {
        super();
    }

    @Override
    public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType) {
        return new String[]{objectName + "." + field + "." + errorCode};
    }

    @Override
    public String[] resolveMessageCodes(String errorCode, String objectName) {
        return new String[]{objectName + "." + errorCode};
    }
}
