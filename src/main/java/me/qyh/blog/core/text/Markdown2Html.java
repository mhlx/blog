package me.qyh.blog.core.text;

import java.util.HashMap;
import java.util.Map;

public interface Markdown2Html {

	/**
	 * 将markdown文本转化为html
	 * 
	 * @param markdown
	 *            md文本
	 * @return
	 */
	String toHtml(String markdown);

	/**
	 * 将多个markdown文本转化为html
	 * 
	 * @param markdownMap
	 *            key:index,v:markdown文本
	 * @return key:index,v:html文本
	 * @since 6.5
	 */
	default Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
		Map<Integer, String> htmlMap = new HashMap<>();
		markdownMap.forEach((k, v) -> {
			String html = toHtml(v);
			if (html != null) {
				htmlMap.put(k, html);
			}
		});
		return htmlMap;
	}

}
