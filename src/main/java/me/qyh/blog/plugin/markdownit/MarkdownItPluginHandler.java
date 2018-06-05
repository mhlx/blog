package me.qyh.blog.plugin.markdownit;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Version;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpRequest.BodyPublisher;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.vo.JsonResult;

/**
 * @see MarkdownItMarkdown2Html
 * @author wwwqyhme
 *
 */
public class MarkdownItPluginHandler extends PluginHandlerSupport {

	private static final String ENABLE_KEY = "plugin.markdownit.enable";
	private static final String URL_KEY = "plugin.markdownit.url";

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final Logger logger = LoggerFactory.getLogger(MarkdownItPluginHandler.class);

	@Override
	protected void registerBean(BeanRegistry registry) {
		HttpClient client = HttpClient.newHttpClient();
		String url = pluginProperties.get(URL_KEY).orElseThrow();
		if (isServiceAvailable(url, client)) {
			registry.register(MarkdownItMarkdown2Html.class.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(MarkdownItMarkdown2Html.class)
							.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(url)
							.addConstructorArgValue(client).getBeanDefinition());
		}
	}

	private boolean isServiceAvailable(String url, HttpClient client) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.warn("无法解析的地址：" + url + "," + e.getMessage(), e);
			return false;
		}
		HttpRequest req = HttpRequest.newBuilder().uri(uri).version(Version.HTTP_1_1)
				.POST(BodyPublisher.fromString("# test")).build();
		try {
			HttpResponse<String> resp = client.send(req, BodyHandler.asString());
			String json = resp.body();
			Jsons.readValue(JsonResult.class, json);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (Exception e) {
			logger.warn("尝试转化markdown失败：" + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean enable() {
		return pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false) && isIncubatorEnable();
	}

	private boolean isIncubatorEnable() {
		try {
			Class.forName("jdk.incubator.http.HttpClient");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
