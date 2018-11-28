package me.qyh.blog.core.service.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class SyncInterceptor {

	@Around("@annotation(Sync)")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		synchronized (pjp.getTarget()) {
			return pjp.proceed();
		}
	}

}
