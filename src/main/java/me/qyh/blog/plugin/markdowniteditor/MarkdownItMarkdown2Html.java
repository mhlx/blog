package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Version;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpRequest.BodyPublisher;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.text.Markdown2Html;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.vo.JsonResult;

class MarkdownItMarkdown2Html implements Markdown2Html {

	private final String url;

	private final HttpClient httpClient;

	public MarkdownItMarkdown2Html(String url, HttpClient httpClient) {
		super();
		this.url = url;
		this.httpClient = httpClient;
	}

	@Override
	public String toHtml(String markdown) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new SystemException(e.getMessage(), e);
		}
		HttpRequest req = HttpRequest.newBuilder().uri(uri).version(Version.HTTP_1_1)
				.POST(BodyPublisher.fromString(markdown)).build();
		try {
			HttpResponse<String> resp = httpClient.send(req, BodyHandler.asString());
			String json = resp.body();
			JsonResult result = Jsons.readValue(JsonResult.class, json);
			if (result.isSuccess()) {
				return Objects.toString(result.getData());
			}
			throw new SystemException("转化markdown失败:" + result);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemException(e.getMessage(), e);
		}
	}

}
