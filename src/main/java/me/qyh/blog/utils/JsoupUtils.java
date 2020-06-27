package me.qyh.blog.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Optional;

public class JsoupUtils {

    private JsoupUtils() {
        super();
    }

    public static Optional<String> getFirstImage(String content) {
        if (StringUtils.isNullOrBlank(content)) {
            return Optional.empty();
        }
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
