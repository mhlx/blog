package me.qyh.blog.security;

public interface PasswordProtect {

    String getResId();

    String getPassword();

    default boolean isHasPassword() {
        return getPassword() != null;
    }

    default void clear() {

    }
}
