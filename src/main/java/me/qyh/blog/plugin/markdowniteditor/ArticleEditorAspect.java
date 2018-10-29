package me.qyh.blog.plugin.markdowniteditor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ui.Model;

import me.qyh.blog.core.entity.Article;

@Aspect
class ArticleEditorAspect {

	@Around("execution(* me.qyh.blog.web.controller.console.ArticleMgrController.write(..))")
	public Object write(ProceedingJoinPoint joinPoint) throws Throwable {
		Object proceed = joinPoint.proceed();
		/**
		 * @since 6.7
		 */
		Model model = (Model) joinPoint.getArgs()[1];
		if (model.asMap().get("article") != null) {
			return "plugin/markdowniteditor/new_md";
		}

		return proceed;
	}

	@Around("execution(* me.qyh.blog.web.controller.console.ArticleMgrController.update(Integer,..))")
	public Object update(ProceedingJoinPoint joinPoint) throws Throwable {
		Model model = (Model) joinPoint.getArgs()[2];
		Object proceed = joinPoint.proceed();
		Article article = (Article) model.asMap().get("article");
		if (article != null) {
			return "plugin/markdowniteditor/new_md";
		}
		return proceed;
	}
}
