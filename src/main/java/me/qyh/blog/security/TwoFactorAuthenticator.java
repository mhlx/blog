package me.qyh.blog.security;

public interface TwoFactorAuthenticator {

    boolean check(String code);

    default void afterUsernamePasswordAuthenticated() {}
}
