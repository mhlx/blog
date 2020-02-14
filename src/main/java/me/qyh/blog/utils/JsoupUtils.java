package me.qyh.blog.utils;

import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupUtils {

	private JsoupUtils() {
		super();
	}

	public static Optional<String> getFirstImage(String content) {
		return getFirstImage(Jsoup.parse(content));
	}

	public static Optional<String> getFirstImage(Document document) {
		return Optional.ofNullable(document.selectFirst("img[src],video[poster]")).map(ele -> {
			if (ele.normalName().equals("img")) {
				return ele.attr("src");
			} else {
				return ele.attr("poster");
			}
		});
	}
}
