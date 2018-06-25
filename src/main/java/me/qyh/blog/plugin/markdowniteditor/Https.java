package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Version;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpRequest.BodyPublisher;
import jdk.incubator.http.HttpResponse.BodyHandler;
import me.qyh.blog.core.exception.SystemException;

/**
 * <p>
 * <b>如果启用了java9的httpclient，则会优先采用，可以在tomcat的启动参数中，加入额外的模块，例如在catalina.sh中加入</b>
 * </p>
 * 
 * <pre>
 * JAVA_OPTS=--add-modules=jdk.incubator.httpclient
 * </pre>
 * 
 * @author wwwqyhme
 *
 */
class Https {

	private static boolean isIncubatorEnable;
	private static HttpTools tools;

	static {
		try {
			Class.forName("jdk.incubator.http.HttpClient");
			isIncubatorEnable = true;
		} catch (Exception e) {
		}
		if (isIncubatorEnable) {
			tools = new IncubatorHttpTools();
		} else {
			tools = new DefaultHttpTools();
		}
	}

	public static String post(String uri, String data) throws IOException {
		return tools.post(uri, data);
	}

	private Https() {
		super();
	}

	private interface HttpTools {
		String post(String uri, String data) throws IOException;
	}

	private static final class IncubatorHttpTools implements HttpTools {

		private HttpClient client;

		public IncubatorHttpTools() {
			super();
			this.client = HttpClient.newHttpClient();
		}

		@Override
		public String post(String uri, String data) throws IOException {
			URI _uri;
			try {
				_uri = new URI(uri);
			} catch (URISyntaxException e) {
				throw new SystemException(e.getMessage(), e);
			}
			HttpRequest req = HttpRequest.newBuilder().uri(_uri).version(Version.HTTP_1_1)
					.POST(BodyPublisher.fromString(data)).build();
			try {
				return client.send(req, BodyHandler.asString()).body();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SystemException(e.getMessage(), e);
			}
		}

	}

	private static final class DefaultHttpTools implements HttpTools {

		@Override
		public String post(String uri, String data) throws IOException {
			URL url = new URL(uri);
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection) con;
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.connect();
			try (OutputStream os = http.getOutputStream()) {
				os.write(data.getBytes(StandardCharsets.UTF_8));
			}
			try (InputStream is = http.getInputStream()) {
				return new String(is.readAllBytes());
			}
		}

	}

}
