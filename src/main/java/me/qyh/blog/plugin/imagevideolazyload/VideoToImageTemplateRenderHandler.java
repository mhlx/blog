package me.qyh.blog.plugin.imagevideolazyload;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;

import me.qyh.blog.template.render.NamedTemplateRenderHandler;

/**
 * &lt;video src="" poster=""&gt; ==> &lt;a href="video_src"&gt;&lt;img
 * src=""/&gt;&lt;/a&gt;
 * 
 * @author wwwqyhme
 *
 */
public class VideoToImageTemplateRenderHandler implements NamedTemplateRenderHandler {

	@Override
	public String name() {
		return "videoToImage";
	}

	@Override
	public String afterRender(String content, HttpServletRequest request, String contentType,
			Map<String, String> attrs) {
		if (!"text/html".equalsIgnoreCase(contentType)) {
			return content;
		}
		Document doc = Jsoup.parse(content);
		doc.select("video[src][poster]").forEach(e -> {
			String poster = e.attr("poster");
			String src = e.attr("src");
			e.after("<a href=\"" + src + "\"><div style=\"position:relative;display: inline-block\"><img src=\""
					+ poster
					+ "\"/><i class=\"fas fa-video \" style=\"position: absolute;left: 50%;top: 50%;font-size:5rem;transform: translate(-50%, -50%);\"></i></div></a>");
			e.remove();
		});
		doc.outputSettings(new OutputSettings().prettyPrint(false));
		return doc.html();
	}

}
