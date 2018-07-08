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

import java.io.Writer;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.template.render.MissLockException;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.RedirectException;
import me.qyh.blog.template.render.TemplateRender;
import me.qyh.blog.web.Webs;

public class TemplateReturnValueHandler implements HandlerMethodReturnValueHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateReturnValueHandler.class);

	private static final String X_PJAX_HEADER_NAME = "X-PJAX";

	private final TemplateRender templateRender;

	public TemplateReturnValueHandler(TemplateRender templateRender) {
		this.templateRender = templateRender;
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return TemplateView.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {
		mavContainer.setRequestHandled(true);
		HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		TemplateView templateView = Objects.requireNonNull((TemplateView) returnValue);

		String templateName = templateView.getTemplateName();

		String contentType = MediaType.TEXT_HTML_VALUE;

		String pattern = templateView.getMatchPattern();
		if (!pattern.isEmpty()) {
			int dotIndex = pattern.lastIndexOf('.');
			if (dotIndex > -1) {
				String ext = pattern.substring(dotIndex + 1).toLowerCase();
				contentType = getContentType(webRequest, ext);
			}
		}

		boolean pjax = Boolean.parseBoolean(webRequest.getHeader(X_PJAX_HEADER_NAME));

		if (pjax) {
			templateName = templateRender.processPjaxTemplateName(templateName, nativeRequest);
			if (!isHtmlContentType(contentType)) {
				throw new LogicException("template.pjax.unsupport", "模板不支持pjax渲染");
			}
		}

		String content;

		try {
			content = templateRender.doRender(templateName, mavContainer.getModel(), nativeRequest,
					new ReadOnlyResponse(nativeResponse), new ParseConfig(false, contentType));

		} catch (RedirectException | MissLockException e) {
			throw e;
		} catch (Exception e) {

			// 解锁页面不能出现异常，不再跳转(防止死循环)
			if (Webs.unlockRequest(nativeRequest) && "GET".equals(nativeRequest.getMethod())) {
				LOGGER.error("在解锁页面发生了一个异常，为了防止死循环，这个页面发生异常将会无法跳转，异常栈信息:" + e.getMessage(), e);
				return;
			}

			throw e;
		}

		nativeResponse.setContentType(contentType);
		nativeResponse.setCharacterEncoding(Constants.CHARSET.name());

		Writer writer = nativeResponse.getWriter();
		writer.write(content);
		writer.flush();
	}

	protected String getContentType(NativeWebRequest request, String ext) {
		if ("html".equals(ext)) {
			return MediaType.TEXT_HTML_VALUE;
		}
		if ("json".equals(ext)) {
			return MediaType.APPLICATION_JSON_VALUE;
		}
		if ("xml".equals(ext)) {
			return MediaType.APPLICATION_XML_VALUE;
		}
		if ("txt".equals(ext)) {
			return MediaType.TEXT_PLAIN_VALUE;
		}
		if ("js".equals(ext)) {
			return "text/javascript";
		}
		if ("css".equals(ext)) {
			return "text/css";
		}
		return "application/octet-stream";
	}

	protected boolean isHtmlContentType(String contentType) {
		return MediaType.TEXT_HTML_VALUE.equals(contentType);
	}
}
