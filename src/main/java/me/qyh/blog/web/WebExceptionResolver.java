/*
 * Copyright 2018 qyh.me
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
package me.qyh.blog.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.support.RequestContextUtils;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.exception.LockException;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.exception.SpaceNotFoundException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.ExceptionHandlerRegistry;
import me.qyh.blog.core.security.AuthencationException;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.util.ExceptionUtils;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.validator.SpaceValidator;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.template.render.MissLockException;
import me.qyh.blog.template.render.RedirectException;
import me.qyh.blog.template.render.TemplateRenderException;
import me.qyh.blog.web.security.csrf.CsrfException;
import me.qyh.blog.web.view.EmptyView;
import me.qyh.blog.web.view.JsonView;

public class WebExceptionResolver implements HandlerExceptionResolver, ExceptionHandlerRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionResolver.class);

	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private SpaceService spaceService;

	public static final Message ERROR_400 = new Message("error.400", "请求异常");
	public static final Message ERROR_403 = new Message("error.403", "权限不足");
	public static final Message ERROR_404 = new Message("error.404", "请求不存在");
	public static final Message ERROR_405 = new Message("error.405", "请求方法不被允许");
	public static final Message ERROR_406 = new Message("error.406", "不被接受的请求");
	public static final Message ERROR_415 = new Message("error.405", "不支持的媒体类型");
	public static final Message ERROR_500 = Constants.SYSTEM_ERROR;

	public static final Message ERROR_MISS_LOCK = new Message("error.missLock", "锁不存在");
	public static final Message ERROR_NO_ERROR_MAPPING = new Message("error.noErrorMapping", "发生了一个错误，但是没有可供显示的错误页面");

	private static final Message UNLOCK_REQUIRE = new Message("unlock.require", "需要解锁资源才能操作");
	private static final Message SPACE_NOT_FOUND = new Message("space.notExists", "空间不存在");

	private final List<ExceptionHandler> handlers;

	public WebExceptionResolver() {
		handlers = new ArrayList<>(Arrays.asList(new AuthencationExceptionHandler(),
				new TemplateRenderExceptionHandler(), new RedirectExceptionHandler(), new LockExceptionHandler(),
				new ResourceNotFoundExceptionHandler(), new LogicExceptionHandler(), new RuntimeLogicExceptionHandler(),
				new SpaceNotFoundExceptionHandler(), new InvalidParamExceptionHandler(),
				new MethodArgumentNotValidExceptionHandler(), new HttpRequestMethodNotSupportedExceptionHandler(),
				new HttpMediaTypeNotAcceptableExceptionHandler(), new HttpMediaTypeNotSupportedExceptionHandler(),
				new MaxUploadSizeExceededExceptionHandler(), new MultipartExceptionHandler(),
				new NoHandlerFoundExceptionHandler(), new MissLockExceptionHandler(), new CsrfExceptionHandler()));
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		for (ExceptionHandler exceptionHandler : handlers) {
			if (exceptionHandler.match(ex)) {
				ModelAndView view = exceptionHandler.handler(request, response, ex);

				if (view == null) {
					throw new SystemException("ExceptionHandler的handler方法不应该返回null");
				}

				return view;
			}
		}

		if (!Webs.isClientAbortException(ex)) {
			String url = UrlUtils.buildFullRequestUrl(request);
			LOGGER.error("[" + url + "]" + ex.getMessage(), ex);
		}

		if (response.isCommitted()) {
			return new ModelAndView();
		}
		if (Webs.isRestful(request)) {
			return restfulView(new RestfulError(ERROR_500), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (Webs.isAjaxRequest(request)) {
			return jsonResultView(new JsonResult(false, ERROR_500));
		}
		return getErrorForward(request, new ErrorInfo(ERROR_500, 500));
	}

	private class AuthencationExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof AuthencationException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(ERROR_403), HttpStatus.FORBIDDEN);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_403));
			}
			String authUrl = null;
			// 将链接放入
			if ("GET".equals(request.getMethod())) {
				authUrl = getFullUrl(request);
			}
			return getErrorForward(request, new Error403Info(ERROR_403, 403, authUrl));
		}
	}

	private final class TemplateRenderExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof TemplateRenderException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			TemplateRenderException tre = (TemplateRenderException) ex;
			tre.writeStackTrace();
			if (!tre.isFromPreview()) {
				LOGGER.error("[" + UrlUtils.buildFullRequestUrl(request) + "]" + ex.getMessage(), ex);
			}
			if (Webs.isRestful(request)) {
				return emptyView(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, tre.getRenderErrorDescription()));
			}
			Map<String, Object> model = new HashMap<>();
			model.put("description", tre.getRenderErrorDescription());

			return new ModelAndView("forward:/error/ui", model);
		}
	}

	private final class RedirectExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof RedirectException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			RedirectException re = (RedirectException) ex;

			if (Webs.isRestful(request)) {
				return restfulView(new RedirectInfo(re.getUrl(), re.isPermanently()), HttpStatus.OK);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new RedirectJsonResult(new RedirectInfo(re.getUrl(), re.isPermanently())));
			}

			response.reset();
			if (re.isPermanently()) {
				// 301
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", re.getUrl());
				return new ModelAndView();
			} else {
				Message redirectMsg = re.getRedirectMsg();
				if (redirectMsg != null) {
					RequestContextUtils.getOutputFlashMap(request).put("redirect_page_msg", redirectMsg);
				}
				ModelAndView mav = new ModelAndView("redirect:" + re.getUrl());
				mav.setStatus(HttpStatus.OK);
				return mav;
			}

		}
	}

	private final class LockExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof LockException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(UNLOCK_REQUIRE), HttpStatus.FORBIDDEN);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, UNLOCK_REQUIRE));
			}
			LockException ex = (LockException) e;
			Lock lock = ex.getLock();
			String redirectUrl = getFullUrl(request);
			Message error = ex.getError();
			if (error != null) {
				RequestContextUtils.getOutputFlashMap(request).put(Constants.ERROR, error);
			}
			return new ModelAndView("redirect:" + Webs.getSpaceUrls(request).getUnlockUrl(lock, redirectUrl));
		}
	}

	private final class ResourceNotFoundExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof ResourceNotFoundException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			ResourceNotFoundException ex = (ResourceNotFoundException) e;
			return handlerLogicException(ex, request);
		}

	}

	private final class LogicExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof LogicException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			LogicException ex = (LogicException) e;
			return handlerLogicException(ex, request);
		}

	}

	private final class RuntimeLogicExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof RuntimeLogicException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			LogicException ex = ((RuntimeLogicException) e).getLogicException();
			return handlerLogicException(ex, request);
		}
	}

	private ModelAndView handlerLogicException(LogicException ex, HttpServletRequest request) {
		HttpStatus status;

		if (ex instanceof ResourceNotFoundException) {
			status = HttpStatus.NOT_FOUND;
		} else {
			status = HttpStatus.CONFLICT;
		}

		if (Webs.isRestful(request)) {
			return restfulView(new RestfulError(ex.getLogicMessage()), status);
		}
		if (Webs.isAjaxRequest(request)) {
			return jsonResultView(new JsonResult(false, ex.getLogicMessage()));
		}

		return getErrorForward(request, new ErrorInfo(ex.getLogicMessage(), status.value()));
	}

	private final class SpaceNotFoundExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof SpaceNotFoundException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			request.removeAttribute(Webs.SPACE_ATTR_NAME);
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(SPACE_NOT_FOUND), HttpStatus.NOT_FOUND);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, SPACE_NOT_FOUND));
			}
			return getErrorForward(request, new ErrorInfo(ERROR_404, 404), null);
		}
	}

	private final class InvalidParamExceptionHandler implements ExceptionHandler {

		private final Class<?>[] exceptionClasses = { BindException.class, HttpMessageNotReadableException.class,
				HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
				MissingServletRequestPartException.class, TypeMismatchException.class,
				/**
				 * @since 6.0
				 */
				ServletRequestBindingException.class,
				/**
				 * @since 7.0
				 */
				InvalidPropertyException.class };

		@Override
		public boolean match(Exception ex) {
			return ExceptionUtils.getFromChain(ex, exceptionClasses).isPresent();
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			LOGGER.debug(ex.getMessage(), ex);

			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(ERROR_400), HttpStatus.BAD_REQUEST);
			}

			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_400));
			}

			return getErrorForward(request, new ErrorInfo(ERROR_400, 400));
		}

	}

	private final class MethodArgumentNotValidExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof MethodArgumentNotValidException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
			BindingResult result = ex.getBindingResult();
			Optional<Message> validateError = Webs.getFirstError(result);
			if (validateError.isPresent()) {
				if (Webs.isRestful(request)) {
					return restfulView(new RestfulError(validateError.get()), HttpStatus.CONFLICT);
				} else {
					return jsonResultView(new JsonResult(false, validateError.get()));
				}
			}
			throw new SystemException("抛出了MethodArgumentNotValidException，但没有发现任何错误");
		}
	}

	private final class HttpRequestMethodNotSupportedExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpRequestMethodNotSupportedException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return emptyView(HttpStatus.METHOD_NOT_ALLOWED);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_405));
			}
			return getErrorForward(request, new ErrorInfo(ERROR_405, 405));
		}

	}

	private final class HttpMediaTypeNotAcceptableExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpMediaTypeNotAcceptableException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(ERROR_406), HttpStatus.NOT_ACCEPTABLE);
			}

			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_406));
			}

			return getErrorForward(request, new ErrorInfo(ERROR_406, 406));
		}

	}

	private final class HttpMediaTypeNotSupportedExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpMediaTypeNotSupportedException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(ERROR_415), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
			}

			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_415));
			}

			return getErrorForward(request, new ErrorInfo(ERROR_415, 415));
		}

	}

	private final class MaxUploadSizeExceededExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof MaxUploadSizeExceededException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
			MaxUploadSizeExceededException ex = (MaxUploadSizeExceededException) e;
			return handlerLogicException(new LogicException(new Message("upload.overlimitsize",
					"超过允许的最大上传文件大小：" + ex.getMaxUploadSize() + "字节", ex.getMaxUploadSize())), request);
		}

	}

	private class NoHandlerFoundExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof NoHandlerFoundException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return emptyView(HttpStatus.NOT_FOUND);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_404));
			}

			// 防止找不到错误页面重定向
			String mapping = request.getServletPath();
			/**
			 * @since 5.9 当nohandlerfound的时候，AppInterceptorHandler不会起作用，因此
			 *        getSpaceFromRequest始终为空，这里需要额外的判断
			 */
			String path = UrlUtils.getRequestURIWithoutContextPath(request);
			String space = Webs.getSpaceFromPath(path, SpaceValidator.MAX_ALIAS_LENGTH + 1);
			String forwardMapping;
			if (space != null) {

				// 检查空间是否存在
				if (SpaceValidator.isValidAlias(space) && spaceService.getSpace(space).isPresent()) {
					forwardMapping = "/space/" + space + "/error/";
				} else {
					forwardMapping = "/error/";
					space = null;
				}

			} else {
				forwardMapping = "/error/";
			}
			if (mapping.startsWith(forwardMapping)) {
				return jsonResultView(new JsonResult(false, ERROR_NO_ERROR_MAPPING));
			} else {
				return getErrorForward(request, new ErrorInfo(ERROR_404, 404), space);
			}
		}

	}

	private final class CsrfExceptionHandler extends AuthencationExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof CsrfException;
		}
	}

	private final class MultipartExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof MultipartException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			if (Webs.isRestful(request)) {
				return restfulView(new RestfulError(ERROR_400), HttpStatus.BAD_REQUEST);
			}
			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_400));
			}
			return new ModelAndView();
		}

	}

	private final class MissLockExceptionHandler implements ExceptionHandler {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof MissLockException;
		}

		@Override
		public ModelAndView handler(HttpServletRequest request, HttpServletResponse response, Exception ex) {

			if (Webs.isRestful(request)) {
				return restfulView(ERROR_MISS_LOCK, HttpStatus.NOT_FOUND);
			}

			if (Webs.isAjaxRequest(request)) {
				return jsonResultView(new JsonResult(false, ERROR_MISS_LOCK));
			}

			return new ModelAndView("redirect:" + urlHelper.getUrl());
		}

	}

	private static ModelAndView restfulView(Object obj, HttpStatus status) {
		ModelAndView mav = new ModelAndView(new JsonView(obj));
		mav.setStatus(status);
		return mav;
	}

	private static ModelAndView jsonResultView(JsonResult result) {
		return new ModelAndView(new JsonView(result));
	}

	private static ModelAndView emptyView(HttpStatus status) {
		ModelAndView mav = new ModelAndView(EmptyView.VIEW);
		mav.setStatus(status);
		return mav;
	}

	public static String getFullUrl(HttpServletRequest request) {
		return UrlUtils.buildFullRequestUrl(request);
	}

	public static ModelAndView getErrorForward(HttpServletRequest request, ErrorInfo error) {
		return getErrorForward(request, error, Webs.getSpaceFromRequest(request));
	}

	public static ModelAndView getErrorForward(HttpServletRequest request, ErrorInfo error, String space) {
		Map<String, Object> model = new HashMap<>();

		/**
		 * 如果仍然包含重定向参数，防止和error冲突
		 */
		request.removeAttribute(DispatcherServlet.INPUT_FLASH_MAP_ATTRIBUTE);
		model.put(Constants.ERROR, error);

		/**
		 * 标记ERROR
		 * 
		 * @since 5.9
		 */
		request.setAttribute(Webs.ERROR_ATTR_NAME, Boolean.TRUE);

		if (space != null) {
			return new ModelAndView("forward:/space/" + space + "/error/" + error.getCode(), model,
					HttpStatus.valueOf(error.getCode()));
		} else {
			return new ModelAndView("forward:/error/" + error.getCode(), model, HttpStatus.valueOf(error.getCode()));
		}
	}

	public class ErrorInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Message message;
		private final int code;

		public ErrorInfo(Message message, int code) {
			super();
			this.message = message;
			this.code = code;
		}

		public Message getMessage() {
			return message;
		}

		public int getCode() {
			return code;
		}

	}

	public class Error403Info extends ErrorInfo {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String url;

		public Error403Info(Message message, int code, String url) {
			super(message, code);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

	}

	public static final class RedirectInfo {
		private final String url;
		private final boolean permanently;

		private RedirectInfo(String url, boolean permanently) {
			super();
			this.url = url;
			this.permanently = permanently;
		}

		public boolean isPermanently() {
			return permanently;
		}

		public String getUrl() {
			return url;
		}
	}

	public static final class RedirectJsonResult extends JsonResult {

		private final RedirectInfo info;

		public RedirectJsonResult(RedirectInfo info) {
			super(true);
			this.info = info;
		}

		public RedirectInfo getInfo() {
			return info;
		}

	}

	@Override
	public ExceptionHandlerRegistry register(ExceptionHandler exceptionHandler) {
		this.handlers.add(exceptionHandler);
		return this;
	}

	public final class RestfulError {
		private final Message error;

		public RestfulError(Message error) {
			super();
			this.error = error;
		}

		public Message getError() {
			return error;
		}
	}

}
