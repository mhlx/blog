package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import me.qyh.blog.core.exception.SystemException;

/**
 * 
 * @author wwwqyhme
 *
 */
class Https {

	private final HttpClient client;

	private static final Https INS = new Https();

	static Https getIns() {
		return INS;
	}

	public String post(String uri, String data) throws IOException {
		URI _uri;
		try {
			_uri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new SystemException(e.getMessage(), e);
		}
		HttpRequest req = HttpRequest.newBuilder().uri(_uri).version(Version.HTTP_1_1)
				.POST(BodyPublishers.ofString(data)).build();
		try {
			return client.send(req, BodyHandlers.ofString()).body();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemException(e.getMessage(), e);
		}
	}

	private Https() {
		super();
		this.client = HttpClient.newHttpClient();
	}

}
