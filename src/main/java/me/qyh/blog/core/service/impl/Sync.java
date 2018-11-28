package me.qyh.blog.core.service.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * 将方法同步，假设有
 * 
 * <pre>
 * public class T {
 * 	&#64;Sync
 * 	public void a() {
 * 	}
 * }
 * </pre>
 * 
 * 那么此时a方法的执行等效于
 * 
 * <pre>
 * synchronized(instance T){
 * 		a();
 * }
 * </pre>
 * <p>
 * <b>用来解决方法同步但aop事务不同步的问题，因此它的优先级必须要比TransactionInterceptor的优先级高</b>
 * </p>
 * 
 * @author Administrator
 * @see SyncInterceptor
 * @see TransactionInterceptor
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Sync {

}
