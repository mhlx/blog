package me.qyh.blog.plugin.markdownit;

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

/**
 * 利用和nodejs的交互来解析markdown，以markdown-it为例：<br>
 * 安装:
 * 
 * <pre>
 * npm install markdown-it --save
 * </pre>
 * 
 * 启动：
 * 
 * <pre>
 * node / path / to / app.js
 * </pre>
 * 
 * app.js:
 * 
 * <pre>
 * const http = require('http');

const hostname = '127.0.0.1';
const port = 3000;

var md = require('markdown-it')({
	html : true,
	linkify: true,
	typographer: true
}).use(require('markdown-it-inject-linenumbers'));

const server = http.createServer((req, res) => {
  if(req.method == 'POST'){
	var body = '';
	req.on('data', function (data) {
		body += data;
	});
	req.on('end', function () {
		body = md.render(body,{});
		var json = JSON.stringify({ 
			success: true, 
			data: body
		});
		
		res.writeHead(200, {'Content-Type': 'application/json'});
		res.end(json);
	});
  }else{
	  
	  var json = JSON.stringify({ 
		success: false, 
		message: "post only"
	  });
	  
	  res.writeHead(200, {'Content-Type': 'application/json'});
	  res.end(json);
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});
 * </pre>
 * 
 * 这样通过 post http://localhost:3000 即可获取转化后的文本
 * 
 * <p>
 * <b>采用了java9的httpclient来简化操作，需要在tomcat的启动参数中，加入额外的模块，例如在catalina.sh中加入</b>
 * </p>
 * 
 * <pre>
 * JAVA_OPTS=--add-modules=jdk.incubator.httpclient
 * </pre>
 * 
 * @author wwwqyhme
 *
 */
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
