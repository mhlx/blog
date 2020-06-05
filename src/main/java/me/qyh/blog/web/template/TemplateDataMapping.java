package me.qyh.blog.web.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.GET)
public @interface TemplateDataMapping {

	@AliasFor(annotation = RequestMapping.class)
	String[] value() default {};

	@AliasFor(annotation = RequestMapping.class)
	String[] params() default {};

	@AliasFor(annotation = RequestMapping.class)
	String[] produces() default {};

}