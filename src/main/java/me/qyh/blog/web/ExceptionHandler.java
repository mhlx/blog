package me.qyh.blog.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public interface ExceptionHandler {
	boolean match(Exception ex);

	ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex);
}
