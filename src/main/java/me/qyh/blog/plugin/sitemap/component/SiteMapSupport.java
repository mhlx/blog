package me.qyh.blog.plugin.sitemap.component;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.UriComponentsBuilder;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.service.impl.Transactions;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.sitemap.SiteUrl;

/**
 * {@link https://www.sitemaps.org/protocol.html}
 * 
 * @author wwwqyhme
 *
 */
@Component
public class SiteMapSupport implements InitializingBean {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private TaskScheduler taskScheduler;

	private ScheduledFuture<?> future;

	private final List<SiteUrlProvider> providers = new ArrayList<>();

	private static final String[] VALID_FREQS = { "always", "hourly", "daily", "weekly", "monthly", "yearly", "never" };
	private static final String CRON_KEY = "plugin.sitemap.refreshCron";

	private String siteMapXml;

	private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

	public String getSiteMapXml() {
		if (siteMapXml == null) {
			synchronized (this) {
				if (siteMapXml == null) {
					refresh();
				}
			}
		}
		return siteMapXml;
	}

	private void refresh() {
		if (providers.isEmpty()) {
			siteMapXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"></urlset>";
			return;
		}
		List<SiteUrl> urls = new ArrayList<>();
		Transactions.executeInReadOnlyTransaction(platformTransactionManager, status -> {
			for (SiteUrlProvider provider : providers) {
				urls.addAll(provider.provide());
			}
		});
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		DecimalFormat priFormat = new DecimalFormat("#0.0");
		Set<String> urlSet = new HashSet<>();
		for (SiteUrl url : urls) {
			Optional<String> encodeLoc = encodeLoc(url.getLoc());
			if (!encodeLoc.isPresent()) {
				continue;
			}
			String loc = encodeLoc.get();
			if (!urlSet.add(loc)) {
				continue;
			}
			sb.append("<url>");
			sb.append("<loc>").append(loc).append("</loc>");
			String freq = url.getChangefreq();
			if (!Validators.isEmptyOrNull(freq, false)) {
				for (String validFreq : VALID_FREQS) {
					if (validFreq.equals(freq)) {
						sb.append("<changefreq>").append(freq).append("</changefreq>");
						break;
					}
				}
			}
			String lastmod = url.getLastmod();
			if (lastmod != null) {
				sb.append("<lastmod>").append(lastmod).append("</lastmod>");
			}
			Double pri = url.getPriority();
			if (pri != null && pri >= 0D && pri <= 1D) {
				sb.append("<priority>").append(priFormat.format(pri)).append("</priority>");
			}
			sb.append("</url>");
		}
		sb.append("</urlset>");
		siteMapXml = sb.toString();
	}

	private Optional<String> encodeLoc(String loc) {
		if (loc == null) {
			return Optional.empty();
		}
		if (!loc.startsWith("http://") && !loc.startsWith("https://")) {
			loc = FileUtils.cleanPath(loc);
			if (loc.isEmpty()) {
				loc = urlHelper.getUrl();
			} else {
				loc = urlHelper.getUrl() + "/" + loc;
			}
		}
		try {
			return Optional.of(UriComponentsBuilder.fromHttpUrl(loc).build().encode().toString());
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	void addSiteUrlProvider(SiteUrlProvider provider) {
		this.providers.add(provider);
	}

	@EventListener
	void start(ContextRefreshedEvent evt) {
		if (evt.getApplicationContext().getParent() == null) {
			return;
		}
		this.providers.addAll(BeanFactoryUtils
				.beansOfTypeIncludingAncestors(evt.getApplicationContext(), SiteUrlProvider.class, true, false)
				.values());
	}

	static String parseDate(Date date) {
		return Times.toLocalDateTime(date).atZone(ZoneId.systemDefault()).format(ISO);
	}

	static String parseDate(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).format(ISO);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String cron = PluginProperties.getInstance().get(CRON_KEY).orElse("0 0 1 * * ?");
		CronTrigger trigger = new CronTrigger(cron);
		future = taskScheduler.schedule(this::refresh, trigger);
	}

	@EventListener
	void close(ContextClosedEvent evt) {
		if (!future.isCancelled()) {
			future.cancel(true);
		}
	}
}
