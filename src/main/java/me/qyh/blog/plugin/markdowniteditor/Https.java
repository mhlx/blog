package me.qyh.blog.plugin.markdowniteditor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import me.qyh.blog.core.util.Resources;

/**
 * 
 * @author wwwqyhme
 *
 */
class Https {

	static String post(String uri, String data) throws IOException {
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

	private Https() {
		super();
	}

}
