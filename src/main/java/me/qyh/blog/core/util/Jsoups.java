package me.qyh.blog.core.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @since 7.0
 * @author wwwqyhme
 *
 */
public class Jsoups {

	private Jsoups() {
		super();
	}

	public static Document body(String bodyHtml) {
		return Jsoup.parseBodyFragment(bodyHtml);
	}

	public static Document html(String html) {
		return Jsoup.parse(html);
	}

}
