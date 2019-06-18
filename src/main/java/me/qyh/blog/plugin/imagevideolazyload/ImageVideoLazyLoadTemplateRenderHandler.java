package me.qyh.blog.plugin.imagevideolazyload;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
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
		String url = urlHelper.getUrl();
		doc.body().append("<script>(function(w, d){\r\n" + 
				"    var b = d.getElementsByTagName('body')[0];\r\n" + 
				"    var s = d.createElement(\"script\"); \r\n" + 
				"    var v = !(\"IntersectionObserver\" in w) ? \"8.17.0\" : \"10.19.0\";\r\n" + 
				"    s.async = true; // This includes the script as async. See the \"recipes\" section for more information about async loading of LazyLoad.\r\n" + 
				"    s.src = \""+url+"/static/plugin/imagevideolazyload/lazyload-\" + v + \".min.js\";\r\n" + 
				"    w.lazyLoadOptions = {elements_selector: \"."+lazyClass+"\"};\r\n" + 
				"    b.appendChild(s);\r\n" + 
				"}(window, document));</script>");
		doc.outputSettings(new OutputSettings().prettyPrint(false));
		return doc.html();
	}

	@Override
	public String name() {
		return "lazy";
	}

}
