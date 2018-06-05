/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.web.view;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRender;

@Component
public class TemplateViewResolver extends AbstractCachingViewResolver {

	private static final String REDIRECT_URL_PREFIX = "redirect:";
	private static final String FORWARD_URL_PREFIX = "forward:";

	@Autowired
	private TemplateRender templateRender;

	@Override
	protected View createView(final String viewName, final Locale locale) throws Exception {
		if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
			final String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length(), viewName.length());
			final RedirectView view = new RedirectView(redirectUrl, true, true);
			return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
		}
		if (viewName.startsWith(FORWARD_URL_PREFIX)) {
			final String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length(), viewName.length());
			return new InternalResourceView(forwardUrl);
		}
		return loadView(viewName, locale);
	}

	@Override
	protected View loadView(String viewName, Locale locale) {
		return new _View(viewName);
	}

	private final class _View implements View {

		private final String templateName;

		_View(String templateName) {
			super();
			this.templateName = templateName;
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			String content = templateRender.doRender(templateName, model, request, new ReadOnlyResponse(response),
					new ParseConfig());

			response.setContentType(getContentType());
			response.setCharacterEncoding(Constants.CHARSET.name());
			response.getWriter().write(content);
			response.getWriter().flush();

		}

	}

}
