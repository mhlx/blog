package me.qyh.blog.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.WebUtils;
import org.thymeleaf.exceptions.TemplateProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.LoginFailException;
import me.qyh.blog.exception.PasswordProtectException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.web.template.TemplateUtils;
import me.qyh.blog.web.template.tag.HttpStatusException;

@Component
public class BlogHandlerExceptionResolver implements HandlerExceptionResolver, ErrorAttributes {

	private static final Logger logger = LoggerFactory.getLogger(BlogHandlerExceptionResolver.class);
	private static final String ERROR_ATTRIBUTES = BlogHandlerExceptionResolver.class.getName() + ".ERROR_ATTRIBUTE";

	private final List<ExceptionResolver> resolvers = new ArrayList<>();
	private static Class<?> clientAbortExceptionClass;

	static {
		try {
			clientAbortExceptionClass = Class.forName("org.apache.catalina.connector.ClientAbortException");
		} catch (ClassNotFoundException ignored) {
		}
	}

	public BlogHandlerExceptionResolver(MultipartProperties multipartProperties) {
		resolvers.add(new ResourceNotFoundExceptionResolver());
		resolvers.add(new LogicExceptionResolver());
		resolvers.add(new PasswordProtectExceptionResolver());
		resolvers.add(new AuthencationExceptionResolver());
		resolvers.add(new BadRequestExceptionResolver());
		resolvers.add(new HttpStatusExceptionResolver());
		resolvers.add(new HttpRequestMethodNotSupportedExceptionResolver());
		resolvers.add(new HttpMediaTypeNotSupportedExceptionResolver());
		resolvers.add(new MultipartExceptionResolver(multipartProperties));
		resolvers.add(new TemplateProcessingExceptionResolver());
		resolvers.add(new LoginFailExceptionResolver());
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (resolve(request, response, ex)) {
			return new ModelAndView();
		}
		boolean isClientAbortExceptionClass = false;
		if (clientAbortExceptionClass != null) {
			Throwable logEx = ex;
			while (logEx != null) {
				if (clientAbortExceptionClass.isAssignableFrom(logEx.getClass())) {
					isClientAbortExceptionClass = true;
					break;
				}
				logEx = logEx.getCause();
			}
		}
		if (!isClientAbortExceptionClass) {
			StringBuilder error = new StringBuilder();
			if (request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) != null) {
				String uri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
				String prmstr = (String) request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
				error.append("url:").append(uri).append(prmstr == null ? "" : "?" + prmstr).append("\n");
			} else {
				error.append("url:").append(ServletUriComponentsBuilder.fromRequest(request).toUriString())
						.append("\n");
			}

			error.append("method:").append(request.getMethod()).append("\n");
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = headerNames.nextElement();
				String value = request.getHeader(key);
				error.append("header[").append(key).append("]:").append(value).append("\n");
			}
			error.append("msg:").append(ex.getMessage());
			logger.error(error.toString(), ex);
		}
		return null;
	}

	public void resolveTemplateException(HttpServletRequest request, HttpServletResponse response,
										 Exception ex) {
		if (!resolve(request, response, ex)) {
			// we can not handle this exception
			// wrap it with TemplateProcessException and resolve it again
			TemplateProcessingException tpe = new TemplateProcessingException(ex.getMessage(), ex);
			resolve(request, response, tpe);
		}
	}

	public void resolveErrorPageException(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
		request.removeAttribute(ERROR_ATTRIBUTES);
		boolean preview = TemplateUtils.isPreviewRequest(request);
		if (preview) {
			try {
				Map<String, Object> attributes = new HashMap<>();
				attributes.put("stackTrace", getStackTrace(ex));
				attributes.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				attributes.put("errors", List.of(new Message("preview.errorPageError", "预览时错误处理页面发生了一个异常")));
				attributes.put("viewName", "errorPageError");
				request.setAttribute(ERROR_ATTRIBUTES, attributes);
				request.getRequestDispatcher("/error").forward(request, response);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.error(ex.getMessage(), ex);
		}
	}

	private boolean resolve(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		request.removeAttribute(ERROR_ATTRIBUTES);
		for (ExceptionResolver resolver : resolvers) {
			if (!resolver.match(ex)) {
				continue;
			}
			int status = resolver.getStatus(request, ex);
			Map<String, Object> errors = resolver.readErrors(request, ex);
			request.setAttribute(ERROR_ATTRIBUTES, errors);
			try {
				response.sendError(status);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return true;
		}
		return false;
	}

	private static class PasswordProtectExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof PasswordProtectException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			PasswordProtectException _ex = (PasswordProtectException) ex;
			String url = ServletUriComponentsBuilder.fromRequest(request).toUriString();
			String id = _ex.getId();

			if (me.qyh.blog.utils.WebUtils.isAjaxRequest(request) || me.qyh.blog.utils.WebUtils.isApiRequest(request)) {
				Map<String, Object> errors = new HashMap<>();
				errors.put("id", id);
				if (_ex.isMissPassword()) {
					errors.put("errors", List.of(new Message("password.require", "需要密码才能访问")));
				} else {
					errors.put("errors", List.of(new Message("password.incorrect", "密码不正确")));
				}
				return errors;
			}

			HttpSession session = request.getSession(false);
			if (session != null) {
				synchronized (WebUtils.getSessionMutex(session)) {
					@SuppressWarnings("unchecked")
					Map<String, String> passwordMap = (Map<String, String>) session
							.getAttribute(Constants.PASSWORD_SESSION_KEY);
					if (passwordMap != null) {
						passwordMap.remove(id);
					}
					session.setAttribute(Constants.PASSWORD_SESSION_KEY, passwordMap);
				}
			}
			if (!_ex.isMissPassword()) {
				return Map.of("viewName", "unlock", "id", id, "url", url, "errors",
						List.of(new Message("password.incorrect", "密码不正确")));
			}
			return Map.of("viewName", "unlock", "id", id, "url", url);
		}

		@Override
		public int getStatus(HttpServletRequest reques, Exception ex) {
			return HttpServletResponse.SC_UNAUTHORIZED;
		}
	}

	private static class MultipartExceptionResolver implements ExceptionResolver {

		private final MultipartProperties multipartProperties;

		public MultipartExceptionResolver(MultipartProperties multipartProperties) {
			super();
			this.multipartProperties = multipartProperties;
		}

		@Override
		public boolean match(Exception ex) {
			return ex instanceof MultipartException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			if (ex instanceof MaxUploadSizeExceededException) {
				long perFile = multipartProperties.getMaxFileSize().toBytes();
				long allFile = multipartProperties.getMaxRequestSize().toBytes();
				if (perFile > 0) {
					String readable = FileUtils.humanReadableByteCountBin(perFile);
					return Map.of("errors", List
							.of(new Message("fileUpload.perFileMaxSize.exceeded", "单个文件最大大小为:" + readable, readable)));
				} else {
					String readable = FileUtils.humanReadableByteCountBin(allFile);
					return Map.of("errors", List
							.of(new Message("fileUpload.requestMaxSize.exceeded", "所有文件最大大小为:" + readable, readable)));
				}
			}
			return Map.of();
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_BAD_REQUEST;
		}
	}

	private static class AuthencationExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof AuthenticationException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of();
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_UNAUTHORIZED;
		}
	}

	private static class HttpStatusExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpStatusException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			HttpStatusException hse = (HttpStatusException) ex;
			if (hse.getError() != null) {
				return Map.of("errors", List.of(hse.getError()));
			}
			return Map.of();
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return ((HttpStatusException) ex).getStatus().value();
		}

	}

	private class TemplateProcessingExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof TemplateProcessingException;
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {

			boolean preview = TemplateUtils.isPreviewRequest(request);
			if (preview) {
				try {
					Map<String, Object> attributes = new HashMap<>();
					attributes.put("stackTrace", getStackTrace(ex));
					attributes.put("errors", List.of(new Message("preview.error", "预览时发生了一个异常")));
					return attributes;
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				return Map.of();
			}
			logger.error(ex.toString(), ex);
			return Map.of();
		}
	}

	private static class LogicExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof LogicException;
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_BAD_REQUEST;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of("errors", List.of(((LogicException) ex).getError()));
		}
	}

	private static class HttpRequestMethodNotSupportedExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpRequestMethodNotSupportedException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of();
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
		}

	}

	private static class ResourceNotFoundExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof ResourceNotFoundException;
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_NOT_FOUND;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of("errors", List.of(((ResourceNotFoundException) ex).getError()));
		}
	}

	private static class HttpMediaTypeNotSupportedExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof HttpMediaTypeNotSupportedException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of();
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
		}

	}

	private static class BadRequestExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return BadRequestReader.isBadRequestException(ex);
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			List<MessageSourceResolvable> errors = BadRequestReader.readErrors(ex);
			return errors.isEmpty() ? Map.of() : Map.of("errors", errors);
		}

		@Override
		public int getStatus(HttpServletRequest request, Exception ex) {
			return HttpServletResponse.SC_BAD_REQUEST;
		}
	}

	private static class LoginFailExceptionResolver implements ExceptionResolver {

		@Override
		public boolean match(Exception ex) {
			return ex instanceof LoginFailException;
		}

		@Override
		public Map<String, Object> readErrors(HttpServletRequest request, Exception ex) {
			return Map.of("errors", List.of(((LoginFailException) ex).getError()));
		}

		@Override
		public int getStatus(HttpServletRequest reques, Exception exception) {
			return HttpServletResponse.SC_UNAUTHORIZED;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
		return (Map<String, Object>) webRequest.getAttribute(ERROR_ATTRIBUTES, RequestAttributes.SCOPE_REQUEST);
	}

	@Override
	public Throwable getError(WebRequest webRequest) {
		return null;
	}

	interface ExceptionResolver {
		boolean match(Exception ex);

		Map<String, Object> readErrors(HttpServletRequest request, Exception ex);

		int getStatus(HttpServletRequest request, Exception exception);
	}

	private String getStackTrace(Throwable e) throws IOException {
		try (Writer writer = new StringWriter(); PrintWriter pw = new PrintWriter(writer)) {
			e.printStackTrace(pw);
			return writer.toString();
		}
	}
}
