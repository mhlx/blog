package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.vo.JsonResult;

class MarkdownItMarkdown2Html implements Markdown2Html {

	private final String url;

	public MarkdownItMarkdown2Html(String url) {
		super();
		this.url = url;
	}

	@Override
	public String toHtml(String markdown) {
		if (markdown == null) {
			return "";
		}
		try {
			String json = Https.post(url, Jsons.write(Map.of(1, markdown)));
			JsonResult result = Jsons.readValue(JsonResult.class, json);
			if (result.isSuccess()) {
				return Objects.toString(((Map<?, ?>) result.getData()).get("1"), "");
			}
			throw new SystemException("转化markdown失败:" + result);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Override
	public Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
		try {
			String json = Https.post(url, Jsons.write(markdownMap));
			JsonResult result = Jsons.readValue(JsonResult.class, json);
			if (result.isSuccess()) {
				Map<Integer, String> map = new HashMap<>();
				Map<?, ?> resultMap = (Map<?, ?>) result.getData();
				resultMap.forEach((k, v) -> {
					map.put(Integer.parseInt(k.toString()), Objects.toString(v, ""));
				});
				return map;
			}
			throw new SystemException("转化markdown失败:" + result);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

}
