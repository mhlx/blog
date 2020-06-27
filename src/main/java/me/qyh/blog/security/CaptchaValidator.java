package me.qyh.blog.security;

public interface CaptchaValidator {

    void validate(String key, String code);

}
