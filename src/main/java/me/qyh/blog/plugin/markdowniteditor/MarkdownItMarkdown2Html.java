package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
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
		try {
			String json = Https.post(url, markdown);
			JsonResult result = Jsons.readValue(JsonResult.class, json);
			if (result.isSuccess()) {
				return Objects.toString(result.getData());
			}
			throw new SystemException("转化markdown失败:" + result);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

}
