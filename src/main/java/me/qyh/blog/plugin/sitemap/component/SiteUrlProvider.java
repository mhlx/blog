package me.qyh.blog.plugin.sitemap.component;

import java.util.List;

import me.qyh.blog.plugin.sitemap.SiteUrl;

/**
 * sitemap地址提供器
 * 
 * @author wwwqyhme
 *
 */
public interface SiteUrlProvider {

	/**
	 * 提供所有的sitemap地址，这个方法会在事务种执行
	 * 
	 * @return
	 */
	List<SiteUrl> provide();

}
