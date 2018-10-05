package me.qyh.blog.plugin.sitemap.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.config.UrlHelper.SpaceUrls;
import me.qyh.blog.core.dao.ArticleDao;
import me.qyh.blog.core.dao.NewsDao;
import me.qyh.blog.core.dao.SpaceDao;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.plugin.sitemap.SiteUrl;
import me.qyh.blog.template.dao.PageDao;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.vo.TemplatePageQueryParam;

/**
 * 
 * @author wwwqyhme
 *
 */
@Component
public class DefaultSiteUrlProvider implements SiteUrlProvider {

	@Autowired
	private ArticleDao articleDao;
	@Autowired
	private PageDao pageDao;
	@Autowired
	private NewsDao newsDao;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private SpaceDao spaceDao;

	private final Map<String, SpaceUrls> urlsCache = new HashMap<>();

	@Override
	public List<SiteUrl> provide() {
		List<SiteUrl> urls = new ArrayList<>();
		addSpaces(urls);
		addArticles(urls);
		addNews(urls);
		addPages(urls);
		urlsCache.clear();
		return urls;
	}

	private void addArticles(List<SiteUrl> urls) {
		ArticleQueryParam param = new ArticleQueryParam();
		param.setCurrentPage(1);
		param.setPageSize(50);
		param.setIgnoreLevel(true);
		param.setQueryLock(true);
		param.setQueryPrivate(false);
		param.setStatus(ArticleStatus.PUBLISHED);
		List<Article> articles;
		while (!(articles = articleDao.selectPage(param)).isEmpty()) {
			param.setCurrentPage(param.getCurrentPage() + 1);
			for (Article article : articles) {
				SiteUrl url = new SiteUrl(urlHelper.getUrls().getUrl(article));
				if (article.getLastModifyDate() != null) {
					url.setLastmod(SiteMapSupport.parseDate(article.getLastModifyDate()));
				} else {
					url.setLastmod(SiteMapSupport.parseDate(article.getPubDate()));
				}
				urls.add(url);
			}
			articles = null;
		}
	}

	private void addNews(List<SiteUrl> urls) {
		SiteUrl main = new SiteUrl();
		main.setLoc(urlHelper.getUrl() + "/news");
		urls.add(main);
		NewsQueryParam param = new NewsQueryParam();
		param.setCurrentPage(1);
		param.setPageSize(50);
		param.setQueryPrivate(false);
		List<News> newsList;
		while (!(newsList = newsDao.selectPage(param)).isEmpty()) {
			param.setCurrentPage(param.getCurrentPage() + 1);
			for (News news : newsList) {
				SiteUrl url = new SiteUrl();
				url.setLoc(urlHelper.getUrls().getUrl(news));
				urls.add(url);
			}
		}
	}

	private void addSpaces(List<SiteUrl> urls) {
		urls.add(new SiteUrl(urlHelper.getUrl(), 1D));
		for (Space space : getAllPublicSpace()) {
			urls.add(new SiteUrl(getSpaceUrls(space.getAlias()).getCurrentUrl(), 0.8D));
		}
	}

	private void addPages(List<SiteUrl> urls) {
		urls.add(new SiteUrl(urlHelper.getUrl() + "/archives"));
		for (Space space : getAllPublicSpace()) {
			urls.add(new SiteUrl(getSpaceUrls(space.getAlias()).getCurrentUrl() + "/archives"));
		}
		TemplatePageQueryParam param = new TemplatePageQueryParam();
		param.setCurrentPage(1);
		param.setPageSize(50);
		List<Page> pages;
		while (!(pages = pageDao.selectPage(param)).isEmpty()) {
			param.setCurrentPage(param.getCurrentPage() + 1);
			for (Page page : pages) {
				if (page.isSpaceGlobal()) {
					String alias = page.getAlias();
					if (alias.indexOf('{') == -1) {
						for (Space space : getAllPublicSpace()) {
							if (alias.isEmpty()) {
								urls.add(new SiteUrl(getSpaceUrls(space.getAlias()).getCurrentUrl()));
							} else {
								urls.add(new SiteUrl(getSpaceUrls(space.getAlias()).getCurrentUrl() + "/" + alias));
							}
						}
					}
				} else {
					String relativePath = page.getRelativePath();
					if (!page.hasPathVariable()) {
						if (relativePath.isEmpty()) {
							urls.add(new SiteUrl(urlHelper.getUrl()));
						} else {
							urls.add(new SiteUrl(urlHelper.getUrl() + "/" + relativePath));
						}
					}
				}
			}
		}
	}

	private SpaceUrls getSpaceUrls(String alias) {
		SpaceUrls fromCache = urlsCache.get(alias);
		if (fromCache == null) {
			fromCache = urlHelper.getUrlsBySpace(alias);
			urlsCache.put(alias, fromCache);
		}
		return fromCache;
	}

	private List<Space> getAllPublicSpace() {
		SpaceQueryParam param = new SpaceQueryParam();
		param.setQueryPrivate(false);
		return spaceDao.selectByParam(param);
	}

}
