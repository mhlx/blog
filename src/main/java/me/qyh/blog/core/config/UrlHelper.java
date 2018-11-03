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
package me.qyh.blog.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.ArticleQueryParam.Sort;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.template.PathTemplate;
import me.qyh.blog.web.Webs;

/**
 * 链接辅助类，用来获取一些对象的访问链接
 * 
 * @author Administrator
 *
 */
@Component
public class UrlHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelper.class);
	private static final String SPACE_IN_URL = "/space/";
	private static final Path CONFIG = FileUtils.HOME_DIR.resolve("blog/app.properties");

	private final Urls urls;
	private final String url;

	private final String scheme;
	private final String domain;
	private final int port;
	private final String contextPath;

	private final CookieHelper cookieHelper = new CookieHelper();

	public UrlHelper() throws IOException {
		Properties pros = new Properties();
		if (FileUtils.exists(CONFIG)) {
			try (InputStream is = Files.newInputStream(CONFIG)) {
				pros.load(is);
			}
		}
		scheme = (String) pros.getOrDefault("app.scheme", "http");
		domain = (String) pros.getOrDefault("app.domain", "localhost");
		port = Integer.parseInt((String) pros.getOrDefault("app.port", "8080"));
		contextPath = FileUtils.cleanPath((String) pros.getOrDefault("app.contextPath", ""));
		StringBuilder sb = new StringBuilder();
		sb.append(scheme).append("://");
		sb.append(domain);
		if (!isDefaultPort()) {
			sb.append(":").append(port);
		}
		if (!contextPath.isEmpty()) {
			sb.append("/").append(contextPath);
		}
		this.url = sb.toString();
		this.urls = new Urls();
	}

	/**
	 * 获取空间地址辅助
	 * 
	 * @param alias
	 * @return
	 */
	public SpaceUrls getUrlsBySpace(String alias) {
		return new SpaceUrls(alias);
	}

	/**
	 * 获取当前请求的链接辅助
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public CurrentEnvUrls getCurrentUrls(HttpServletRequest request, HttpServletResponse response) {
		return new CurrentEnvUrls(request, response);
	}

	public Urls getUrls() {
		return urls;
	}

	public String getUrl() {
		return url;
	}

	public String getSchema() {
		return scheme;
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	/**
	 * 不以 / 开头
	 * 
	 * @return
	 */
	public String getContextPath() {
		return contextPath;
	}

	public boolean isDefaultPort() {
		if ("https".equalsIgnoreCase(scheme)) {
			return 443 == port;
		}
		return "http".equalsIgnoreCase(scheme) && 80 == port;
	}

	public boolean isSecure() {
		return "https".equalsIgnoreCase(scheme);
	}

	public CookieHelper getCookieHelper() {
		return cookieHelper;
	}

	/**
	 * 链接辅助类，用来获取配置的域名，根域名，链接，空间访问链接、文章链接等等
	 * 
	 * @author Administrator
	 *
	 */
	public class Urls {

		private Urls() {
			super();
		}

		/**
		 * 获取配置的域名
		 * 
		 * @return
		 */
		public String getDomain() {
			return domain;
		}

		/**
		 * 获取系统主页地址(scheme://domain:port/contextPath)
		 * 
		 * @return
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * 获取空间的访问链接
		 * 
		 * @param space
		 *            空间(别名不能为空)
		 * @return 访问链接
		 */
		public String getUrl(Space space) {
			if (space == null) {
				return url;
			}
			return url + SPACE_IN_URL + space.getAlias();
		}

		/**
		 * 得到博客访问地址
		 * 
		 * @param article
		 * @return
		 */
		public String getUrl(Article article) {
			String idOrAlias = article.getAlias() == null ? String.valueOf(article.getId()) : article.getAlias();
			return getUrl(article.getSpace()) + "/article/" + idOrAlias;
		}

		/**
		 * 获取PathTemplate的访问路径
		 * <p>
		 * <b>不会替换PathVariable中的参数</b>
		 * </p>
		 * 
		 * @param pathTemplate
		 * @return 访问路径
		 */
		public String getUrl(PathTemplate pathTemplate) {
			String relativePath = pathTemplate.getRelativePath();
			if (relativePath.isEmpty()) {
				return url;
			}
			return url + '/' + relativePath;
		}

		/**
		 * 获取动态的详情地址
		 * 
		 * @param news
		 * @return
		 */
		public String getUrl(News news) {
			return url + "/news/" + news.getId();
		}

		/**
		 * 根据相对地址获取完整的地址
		 * 
		 * @since 7.0
		 * @param relativeUrl
		 * @return
		 */
		public String getUrl(String relativeUrl) {
			return url + "/" + FileUtils.cleanPath(relativeUrl);
		}

		public NewsUrlHelper getNewsUrlHelper() {
			return new NewsUrlHelper(url, "news");
		}

		public NewsUrlHelper getNewsUrlHelper(String path) {
			return new NewsUrlHelper(url, path);
		}

		public String getFullUrl(HttpServletRequest request) {
			return UrlUtils.buildFullRequestUrl(request);
		}

		public CookieHelper getCookieHelper() {
			return cookieHelper;
		}

	}

	/**
	 * 当前请求的链接辅助类
	 * 
	 * @author Administrator
	 *
	 */
	public class SpaceUrls extends Urls {

		private String space;
		private String url;

		protected SpaceUrls(String alias) {
			space = alias;
			if (space != null) {
				url = UrlHelper.this.url + SPACE_IN_URL + space;
			} else {
				url = UrlHelper.this.url;
			}
		}

		public String getUnlockUrl(Lock lock, String redirectUrl) {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/unlock/" + lock.getLockType())
					.queryParam("lockId", lock.getId());
			if (redirectUrl != null) {
				builder.queryParam("redirectUrl", redirectUrl);
			}
			return builder.build().toString();
		}

		public String getSpace() {
			return space;
		}

		public String getCurrentUrl() {
			return url;
		}

		/**
		 * 获取指定路径的文章分页链接辅助
		 * 
		 * @param path
		 * @return
		 */
		public ArticlesUrlHelper getArticlesUrlHelper(String path) {
			return new ArticlesUrlHelper(url, path);
		}

		public ArticlesUrlHelper getArticlesUrlHelper() {
			return getArticlesUrlHelper("");
		}
	}

	protected final class NewsUrlHelper {
		private final String url;
		private final String path;

		NewsUrlHelper(String url, String path) {
			super();
			this.url = url;
			this.path = path;
		}

		public String getNewsUrl(NewsQueryParam param, int page) {
			StringBuilder sb = new StringBuilder(url);
			if (!path.isEmpty()) {
				if (!path.startsWith("/")) {
					sb.append('/');
				}
				sb.append(path);
			}
			sb.append("?currentPage=").append(page);
			Date begin = param.getBegin();
			Date end = param.getEnd();
			if (begin != null && end != null) {
				sb.append("&begin=").append(Times.format(Times.toLocalDateTime(begin), "yyyy-MM-dd HH:mm:ss"));
				sb.append("&end=").append(Times.format(Times.toLocalDateTime(end), "yyyy-MM-dd HH:mm:ss"));
			}
			sb.append("&asc=").append(param.isAsc());
			return sb.toString();
		}

		public String getNewsUrl(String begin, String end) {
			NewsQueryParam param = new NewsQueryParam();
			param.setBegin(Times.parseAndGetDate(begin));
			if (param.getBegin() != null) {
				param.setEnd(Times.parseAndGetDate(end));
			}
			return getNewsUrl(param, 1);
		}

		public String getNewsUrl(Date begin, Date end) {
			NewsQueryParam param = new NewsQueryParam();
			param.setBegin(begin);
			param.setEnd(end);
			return getNewsUrl(param, 1);
		}
	}

	protected final class ArticlesUrlHelper {

		private final String url;
		private final String path;

		ArticlesUrlHelper(String url, String path) {
			super();
			this.url = url;
			this.path = path;
		}

		/**
		 * 得到标签的访问链接
		 * 
		 * @param tag
		 *            标签，标签名不能为空！
		 * @return 标签访问链接
		 */
		public String getArticlesUrl(Tag tag) {
			return getArticlesUrl(tag.getName());
		}

		/**
		 * 得到标签的访问地址
		 * 
		 * @param tag
		 *            标签名，会自动过滤html标签，eg:&lt;b&gt;spring&lt;/b&gt;会被过滤为spring
		 * @return 标签访问地址
		 */
		public String getArticlesUrl(String tag) {
			ArticleQueryParam param = new ArticleQueryParam();
			param.setTag(Jsoup.clean(tag, Whitelist.none()));
			return getArticlesUrl(param, 1);
		}

		/**
		 * 根据排序获取分页链接
		 * 
		 * @param param
		 *            当前分页参数
		 * @param sortStr
		 *            排序方式 ，见{@code ArticleQueryParam.Sort}
		 * @return 分页链接
		 */
		public String getArticlesUrl(ArticleQueryParam param, String sortStr) {
			ArticleQueryParam cloned = new ArticleQueryParam(param);
			if (sortStr != null) {
				Sort sort = null;
				try {
					sort = Sort.valueOf(sortStr);
				} catch (Exception e) {
					LOGGER.debug("无效的ArticleQueryParam.Sort:" + sortStr, e);
				}
				cloned.setSort(sort);
			} else {
				cloned.setSort(null);
			}
			return getArticlesUrl(cloned, 1);
		}

		/**
		 * 获取文章分页查询链接
		 * 
		 * @param param
		 *            分页参数
		 * @param page
		 *            当前页面
		 * @return 某个页面的分页链接
		 */
		public String getArticlesUrl(ArticleQueryParam param, int page) {
			StringBuilder sb = new StringBuilder(url);
			if (!path.isEmpty()) {
				if (!path.startsWith("/")) {
					sb.append('/');
				}
				sb.append(path);
			}
			sb.append("?currentPage=").append(page);
			Date begin = param.getBegin();
			Date end = param.getEnd();
			if (begin != null && end != null) {
				sb.append("&begin=").append(Times.format(Times.toLocalDateTime(begin), "yyyy-MM-dd HH:mm:ss"));
				sb.append("&end=").append(Times.format(Times.toLocalDateTime(end), "yyyy-MM-dd HH:mm:ss"));
			}
			if (param.getFrom() != null) {
				sb.append("&from=").append(param.getFrom().name());
			}
			if (param.getStatus() != null) {
				sb.append("&status=").append(param.getStatus().name());
			}
			if (param.getQuery() != null) {
				sb.append("&query=").append(param.getQuery());
			}
			if (param.getTag() != null) {
				sb.append("&tag=").append(param.getTag());
			}
			if (param.getSort() != null) {
				sb.append("&sort=").append(param.getSort().name());
			}
			if (param.hasQuery()) {
				sb.append("&highlight=").append(param.isHighlight() ? "true" : "false");
			}
			if (!CollectionUtils.isEmpty(param.getSpaces())) {
				sb.append("&spaces=").append(param.getSpaces().stream().collect(Collectors.joining(",")));
			}
			return sb.toString();
		}

		/**
		 * 获取某个时间段内文章分页查询链接
		 * 
		 * @param begin
		 *            开始时间
		 * @param end
		 *            结束时间
		 * @return 该时间段内的分页链接
		 */
		public String getArticlesUrl(Date begin, Date end) {
			ArticleQueryParam param = new ArticleQueryParam();
			param.setBegin(begin);
			param.setEnd(end);
			return getArticlesUrl(param, 1);
		}

		/**
		 * 获取某个时间段内文章分页查询链接
		 * 
		 * @param begin
		 *            开始时间
		 * @param end
		 *            结束时间
		 * @return 该时间段内的分页链接
		 */
		public String getArticlesUrl(String begin, String end) {
			ArticleQueryParam param = new ArticleQueryParam();
			param.setBegin(Times.parseAndGetDate(begin));
			if (param.getBegin() != null) {
				param.setEnd(Times.parseAndGetDate(end));
			}
			return getArticlesUrl(param, 1);
		}
	}

	public final class CurrentEnvUrls extends SpaceUrls {

		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private final CurrentCookieHelper cookieHelper;

		public CurrentEnvUrls(HttpServletRequest request, HttpServletResponse response) {
			super(Webs.getSpaceFromRequest(request));
			this.request = request;
			this.response = response;
			this.cookieHelper = new CurrentCookieHelper();
		}

		public String getFullUrl() {
			return super.getFullUrl(request);
		}

		@Override
		public CookieHelper getCookieHelper() {
			return this.cookieHelper;
		}

		public final class CurrentCookieHelper extends CookieHelper {

			public Cookie getCookie(String name) {
				return super.getCookie(name, request);
			}

			public void setCookie(String name, String value, int maxAge) {
				super.setCookie(name, value, maxAge, request, response);
			}

			public void addCookie(String name, String value, int maxAge) {
				super.addCookie(name, value, maxAge, request, response);
			}

			public void deleteCookie(String name) {
				super.deleteCookie(name, request, response);
			}

		}

	}

	public class CookieHelper {
		public void setCookie(String name, String value, int maxAge, HttpServletRequest request,
				HttpServletResponse response) {
			this.setCookie(name, value, maxAge, true, request, response);
		}

		public void setCookie(String name, String value, int maxAge, boolean update, HttpServletRequest request,
				HttpServletResponse response) {
			Cookie cookie = request == null ? null : WebUtils.getCookie(request, name);
			if (cookie == null) {
				cookie = new Cookie(name, value);
			} else {
				if (!update) {
					return;
				}
				cookie.setValue(value);
			}
			cookie.setMaxAge(maxAge);
			cookie.setHttpOnly(true);
			cookie.setSecure(isSecure());
			cookie.setPath("/" + contextPath);
			// cookie.setDomain(domain);
			response.addCookie(cookie);
		}

		public void deleteCookie(String name, HttpServletRequest request, HttpServletResponse response) {
			Cookie cookie = WebUtils.getCookie(request, name);
			if (cookie != null) {
				cookie.setValue(null);
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}

		public Cookie getCookie(String name, HttpServletRequest request) {
			return WebUtils.getCookie(request, name);
		}

		public void addCookie(String name, String value, int maxAge, HttpServletRequest request,
				HttpServletResponse response) {
			setCookie(name, value, maxAge, false, request, response);
		}

	}
}
