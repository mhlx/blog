package me.qyh.blog.plugin.imagevideolazyload;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.template.render.NamedTemplateRenderHandler;

public class ImageVideoLazyLoadTemplateRenderHandler implements NamedTemplateRenderHandler {

	@Autowired
	private UrlHelper urlHelper;

	@Override
	public String afterRender(String content, HttpServletRequest request, String contentType,
			Map<String, String> attrs) {
		if (urlHelper == null) {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		}
		if (!"text/html".equalsIgnoreCase(contentType)) {
			return content;
		}
		String containerClass = attrs.getOrDefault("containerClass", "lazy-container");
		String lazyClass = attrs.getOrDefault("lazyClass", "lazy");
		String versionNum = attrs.get("version");
		Document doc = Jsoup.parse(content);
		doc.select("." + containerClass).forEach(c -> {
			c.select("img[src]").forEach(i -> {
				i.attr("data-src", i.attr("src"));
				i.removeAttr("src");
				i.addClass(lazyClass);
			});

			c.select("video").forEach(v -> {
				String src = v.attr("src");
				v.addClass(lazyClass);
				if (!src.isEmpty()) {
					v.attr("data-src", src);
					v.removeAttr("src");
				}

				// v.select("source").forEach(s -> {
				// s.attr("data-src", s.attr("src"));
				// s.removeAttr("src");
				// });
			});
		});
		String scriptUrl = urlHelper.getUrls().getUrl("/static/plugin/imagevideolazyload/lazyload.min.js");
		if (versionNum != null) {
			scriptUrl += "?version=" + versionNum;
		}
		doc.body().append("<script src=\"" + scriptUrl + "\" type=\"text/javascript\"></script>");
		doc.body()
				.append("<script>\r\n" + "		  (function () {\r\n" + "				new LazyLoad({\r\n"
						+ "					elements_selector: '." + lazyClass + "',\r\n" + "				});\r\n"
						+ "		  }());\r\n" + "		  </script>");
		return doc.html();

	}

	@Override
	public String name() {
		return "lazy";
	}

}
