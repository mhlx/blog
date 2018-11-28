package me.qyh.blog.core.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import me.qyh.blog.core.context.Environment;

/**
 * 用于验证{@code EnsureLogin}的aop
 * 
 * @author Administrator
 *
 */
@Component
@Aspect
public class EnsureLoginAspect {

	@Before(value = "@within(EnsureLogin) || @annotation(EnsureLogin)")
	public void before() {
		Environment.doAuthencation();
	}

}
