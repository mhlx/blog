package me.qyh.blog.web.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.qyh.blog.web.interceptor.AppInterceptor;

/**
 * 忽略校验空间的锁
 * 
 * @since 6.2
 * @see AppInterceptor
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IgnoreSpaceLock {

}
