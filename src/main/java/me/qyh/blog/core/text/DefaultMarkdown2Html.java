package me.qyh.blog.core.text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.vo.JsonResult;

/**
 * @since 7.0
 * @author wwwqyhme
 *
 */
public class DefaultMarkdown2Html implements Markdown2Html, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMarkdown2Html.class);

	private String url;// node地址
	private Markdown2Html delegate;

	@Override
	public String toHtml(String markdown) {
		return delegate.toHtml(markdown);
	}

	@Override
	public Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
		return delegate.toHtmls(markdownMap);
	}

	private String post(String uri, String data) throws IOException {
		URL url = new URL(uri);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		try (OutputStream os = http.getOutputStream();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
			bw.write(data);
		}
		try (InputStream is = http.getInputStream()) {
			return Resources.read(is);
		}
	}

	private boolean isServiceAvailable(String url) {
		try {
			String json = post(url, Jsons.write(Map.of(1, "# Hello World")));
			Jsons.readValue(JsonResult.class, json);
			return true;
		} catch (IOException e) {
			logger.warn("尝试转化markdown失败：" + e.getMessage(), e);
			return false;
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (url != null) {
			if (isServiceAvailable(url)) {
				this.delegate = new MarkdownItMarkdown2Html(url);
			}
		}
		if (this.delegate == null) {
			logger.warn("没有采用MarkdownIt来渲染markdown文本，这将导致预期渲染内容和实际渲染内容存在差异");
			this.delegate = new CommonMarkdown2Html();
		}
	}

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
				String json = post(url, Jsons.write(Map.of(1, markdown)));
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
				String json = post(url, Jsons.write(markdownMap));
				JsonResult result = Jsons.readValue(JsonResult.class, json);
				if (result.isSuccess()) {
					Map<Integer, String> map = new HashMap<>();
					Map<?, ?> resultMap = (Map<?, ?>) result.getData();
					resultMap.forEach((k, v) -> map.put(Integer.parseInt(k.toString()), Objects.toString(v, "")));
					return map;
				}
				throw new SystemException("转化markdown失败:" + result);
			} catch (IOException e) {
				throw new SystemException(e.getMessage(), e);
			}
		}

	}

	class CommonMarkdown2Html implements Markdown2Html {

		private final Parser parser;
		private final HtmlRenderer renderer;

		private final List<Extension> baseExtensions = List.of(AutolinkExtension.create(), TablesExtension.create(),
				StrikethroughExtension.create(), HeadingAnchorExtension.create());

		private CommonMarkdown2Html() {
			parser = Parser.builder().extensions(baseExtensions).build();
			renderer = HtmlRenderer.builder().extensions(baseExtensions).build();
		}

		@Override
		public String toHtml(String markdown) {
			if (markdown == null) {
				return "";
			}
			Node document = parser.parse(markdown);
			return renderer.render(document);
		}

	}

}
