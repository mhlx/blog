package me.qyh.blog.core.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来判断用户是否登录，如果没有登录，则抛出一个{@code AuthencationException}异常
 * 
 * @author Administrator
 * @see AuthencationException
 * @see EnsureLoginAspect
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnsureLogin {

}
