package me.qyh.blog.web.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.context.LockKeyContext;
import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LockException;
import me.qyh.blog.core.exception.SpaceNotFoundException;
import me.qyh.blog.core.security.AuthencationException;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.validator.SpaceValidator;
import me.qyh.blog.web.LockHelper;
import me.qyh.blog.web.RememberMeService;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.security.IgnoreSpaceLock;
import me.qyh.blog.web.security.RequestMatcher;
import me.qyh.blog.web.security.csrf.CsrfException;
import me.qyh.blog.web.security.csrf.CsrfToken;
import me.qyh.blog.web.security.csrf.CsrfTokenRepository;
import me.qyh.blog.web.security.csrf.InvalidCsrfTokenException;
import me.qyh.blog.web.security.csrf.MissingCsrfTokenException;

public class AppInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppInterceptor.class);

	@Autowired
	private SpaceService spaceService;
	@Autowired
	private LockManager lockManager;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private RememberMeService rememberMeService;
	@Autowired(required = false)
	private GoogleAuthenticator ga;

	private static final String UNLOCK_PATTERN = "/unlock/*";
	private static final String SPACE_UNLOCK_PATTERN = "/space/*/unlock/*";

	@Autowired(required = false)
	private CsrfTokenRepository tokenRepository;

	private final CsrfToken emptyToken = new CsrfToken("");

	private RequestMatcher requireCsrfProtectionMatcher = new DefaultRequiresCsrfMatcher();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (isHandler(handler)) {

			HandlerMethod handlerMethod = (HandlerMethod) handler;

			try {
				setRequestAttribute(handlerMethod, request, response);
				setUser(request, response, handlerMethod);
				setLockKeys(request);
				setSpace(request, handlerMethod);
				Environment.setIP(Webs.getIP(request));
				Environment.setPreview(Webs.isPreview(request));
				csrfCheck(request, response);
			} catch (AuthencationException | LockException | SpaceNotFoundException | CsrfException e) {
				removeContext();
				throw e;
			} catch (Throwable e) {
				removeContext();
				LOGGER.error(e.getMessage(), e);
				return false;
			}

		}
		return true;
	}

	private void setUser(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
		HttpSession session = request.getSession(false);
		User user = null;
		if (session != null) {
			user = (User) session.getAttribute(Constants.USER_SESSION_KEY);
		}

		if (user == null) {

			Optional<User> autoLogin = rememberMeService.autoLogin(request, response);
			if (autoLogin.isPresent()) {
				if (session == null) {
					session = request.getSession();
				}
				User _user = autoLogin.get();
				if (ga != null) {
					session.setAttribute(Constants.GA_SESSION_KEY, _user);
				} else {
					session.setAttribute(Constants.USER_SESSION_KEY, _user);
					request.changeSessionId();
					if (tokenRepository != null) {
						tokenRepository.changeToken(request, response);
					}
					user = _user;
				}
			}
		}

		Environment.setUser(user);
		enableLogin(handler);
	}

	private void enableLogin(HandlerMethod methodHandler) {
		// auth check
		getAnnotation(methodHandler.getMethod(), EnsureLogin.class).ifPresent(ann -> Environment.doAuthencation());
	}

	private void setSpace(HttpServletRequest request, HandlerMethod handlerMethod) throws SpaceNotFoundException {
		String spaceAlias = Webs.getSpaceFromRequest(request);
		if (spaceAlias != null) {
			if (!SpaceValidator.isValidAlias(spaceAlias)) {
				throw new SpaceNotFoundException(spaceAlias);
			}
			Space space = spaceService.getSpace(spaceAlias).orElseThrow(() -> new SpaceNotFoundException(spaceAlias));

			if (!Webs.errorRequest(request)) {
				if (space.getIsPrivate()) {
					Environment.doAuthencation();
				}
				if (space.hasLock() && !Webs.unlockRequest(request)
						&& !getAnnotation(handlerMethod.getMethod(), IgnoreSpaceLock.class).isPresent()) {
					lockManager.openLock(space.getLockId());
				}
			}

			Environment.setSpace(space);
		}
	}

	/**
	 * 将session中的解锁钥匙放入上下文中
	 * 
	 * @param request
	 */
	private void setLockKeys(HttpServletRequest request) {
		List<LockKey> keys = LockHelper.getKeys(request);
		if (!CollectionUtils.isEmpty(keys)) {
			LOGGER.debug("将LockKey放入LockKeyContext中:{}", keys);
			LockKeyContext.set(keys);
		}
	}

	private void csrfCheck(HttpServletRequest request, HttpServletResponse response) {
		if (tokenRepository != null) {
			CsrfToken csrfToken = tokenRepository.loadToken(request);
			final boolean missingToken = csrfToken == null;
			if (missingToken) {
				CsrfToken generatedToken = tokenRepository.generateToken(request);
				csrfToken = new SaveOnAccessCsrfToken(tokenRepository, request, response, generatedToken);
			}
			request.setAttribute(CsrfToken.class.getName(), csrfToken);
			request.setAttribute(csrfToken.getParameterName(), csrfToken);
			if ("GET".equals(request.getMethod())) {
				// GET请求不能检查，否则死循环
				return;
			}

			/**
			 * @since 5.9
			 */
			if (Webs.errorRequest(request)) {
				return;
			}

			if (!requireCsrfProtectionMatcher.match(request)) {
				return;
			}
			String actualToken = request.getHeader(csrfToken.getHeaderName());
			if (actualToken == null) {
				actualToken = request.getParameter(csrfToken.getParameterName());
			}
			if (!csrfToken.getToken().equals(actualToken)) {
				if (missingToken) {
					throw new MissingCsrfTokenException(actualToken);
				} else {
					throw new InvalidCsrfTokenException(csrfToken, actualToken);
				}
			}
		} else {
			request.setAttribute(CsrfToken.class.getName(), emptyToken);
			request.setAttribute(emptyToken.getParameterName(), emptyToken);
		}
	}

	private <T extends Annotation> Optional<T> getAnnotation(Method method, Class<T> annotationType) {
		T t = AnnotationUtils.findAnnotation(method, annotationType);
		if (t == null) {
			t = AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationType);
		}
		return Optional.ofNullable(t);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		removeContext();
	}

	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		removeContext();
	}

	private void removeContext() {
		Environment.remove();
		LockKeyContext.remove();
	}

	private void setRequestAttribute(HandlerMethod methodHandler, HttpServletRequest request,
			HttpServletResponse response) {
		if (request.getAttribute(Webs.SPACE_ATTR_NAME) == null) {
			String path = UrlUtils.getRequestURIWithoutContextPath(request);
			if (path.startsWith("api/")) {
				path = path.substring(4);
			}
			request.setAttribute(Webs.SPACE_ATTR_NAME,
					Objects.toString(Webs.getSpaceFromPath(path, SpaceValidator.MAX_ALIAS_LENGTH + 1), ""));
		}
		String alias = Webs.getSpaceFromRequest(request);
		String unlockPattern = alias == null ? UNLOCK_PATTERN : SPACE_UNLOCK_PATTERN;
		if (request.getAttribute(Webs.UNLOCK_ATTR_NAME) == null) {
			String path = "/" + UrlUtils.getRequestURIWithoutContextPath(request);
			boolean isUnlock = UrlUtils.match(unlockPattern, path) && request.getParameter("lockId") != null;
			request.setAttribute(Webs.UNLOCK_ATTR_NAME, isUnlock);
		}
		if (request.getAttribute(Webs.SPACE_URLS_ATTR_NAME) == null) {
			request.setAttribute(Webs.SPACE_URLS_ATTR_NAME, urlHelper.getCurrentUrls(request, response));
		}
	}

	private boolean isHandler(Object handler) {
		return handler instanceof HandlerMethod;
	}

	public void setRequireCsrfProtectionMatcher(RequestMatcher requireCsrfProtectionMatcher) {
		this.requireCsrfProtectionMatcher = requireCsrfProtectionMatcher;
	}

	@SuppressWarnings("serial")
	private static final class SaveOnAccessCsrfToken extends CsrfToken {
		private transient CsrfTokenRepository tokenRepository;
		private transient HttpServletRequest request;
		private transient HttpServletResponse response;

		private final CsrfToken delegate;

		SaveOnAccessCsrfToken(CsrfTokenRepository tokenRepository, HttpServletRequest request,
				HttpServletResponse response, CsrfToken delegate) {
			super(null);
			this.tokenRepository = tokenRepository;
			this.request = request;
			this.response = response;
			this.delegate = delegate;
		}

		@Override
		public String getToken() {
			saveTokenIfNecessary();
			return delegate.getToken();
		}

		@Override
		public String toString() {
			return "SaveOnAccessCsrfToken [delegate=" + delegate + "]";
		}

		@Override
		public int hashCode() {
			return Objects.hash(delegate);
		}

		@Override
		public boolean equals(Object obj) {
			if (Validators.baseEquals(this, obj)) {
				SaveOnAccessCsrfToken other = (SaveOnAccessCsrfToken) obj;
				return Objects.equals(this.delegate, other.delegate);
			}
			return false;
		}

		private void saveTokenIfNecessary() {
			if (this.tokenRepository == null) {
				return;
			}

			synchronized (this) {
				if (tokenRepository != null) {
					this.tokenRepository.saveToken(delegate, request, response);
					this.tokenRepository = null;
					this.request = null;
					this.response = null;
				}
			}
		}

	}

	private static final class DefaultRequiresCsrfMatcher implements RequestMatcher {
		private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.security.web.util.matcher.RequestMatcher#matches(
		 * javax.servlet.http.HttpServletRequest)
		 */
		public boolean match(HttpServletRequest request) {
			return !allowedMethods.matcher(request.getMethod()).matches();
		}
	}
}
