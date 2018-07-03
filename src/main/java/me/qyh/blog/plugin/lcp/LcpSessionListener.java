package me.qyh.blog.plugin.lcp;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.template.service.TemplateService;

public class LcpSessionListener implements HttpSessionListener {

	@Autowired
	private TemplateService templateService;

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		if (session.getAttribute(Constants.USER_SESSION_KEY) != null) {
			templateService.clearPreview();
		}
	}

}
