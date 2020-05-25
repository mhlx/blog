package me.qyh.blog.web.template.expression;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.qyh.blog.utils.JsoupUtils;

final class Jsoups {

	public Document body(String bodyHtml) {
		return Jsoup.parseBodyFragment(bodyHtml);
	}

	public Document html(String html) {
		return Jsoup.parse(html);
	}

	public Document body(String bodyHtml, String baseUri) {
		return Jsoup.parseBodyFragment(bodyHtml, baseUri);
	}

	public Document html(String html, String baseUri) {
		return Jsoup.parse(html, baseUri);
	}

	public DocumentParser parser(Document doc) {
		return new DocumentParser(doc);
	}

	public final class DocumentParser {
		private final Document document;

		private DocumentParser(Document document) {
			super();
			this.document = document;
		}

		/**
		 * 获取纯文本内容
		 * 
		 * @return
		 */
		public String getText() {
			return document.text();
		}

		/**
		 * 获取所有图片的地址
		 * 
		 * <pre>
		 * &lt;img src=""/&gt;
		 * </pre>
		 * 
		 * @return
		 */
		public List<String> getImages() {
			return document.select("img[src]").stream().map(ele -> ele.attr("src")).collect(Collectors.toList());
		}

		/**
		 * 获取第一张图片标签的地址
		 * 
		 * <pre>
		 * &lt;img src=""/&gt;
		 * </pre>
		 * 
		 * @return
		 */
		public Optional<String> getImage() {
			return Optional.ofNullable(document.selectFirst("img[src]")).map(ele -> ele.attr("src"));
		}

		/**
		 * 获取所有视频封面的地址
		 * 
		 * <pre>
		 * &lt;video poster=""/&gt;
		 * </pre>
		 * 
		 * @return
		 */
		public List<String> getVideoPosters() {
			return document.select("video[poster]").stream().map(ele -> ele.attr("poster"))
					.collect(Collectors.toList());
		}

		/**
		 * 获取第一个视频封面的地址
		 * 
		 * <pre>
		 * &lt;video poster=""/&gt;
		 * </pre>
		 * 
		 * @return
		 */
		public Optional<String> getVideoPoster() {
			return Optional.ofNullable(document.selectFirst("video[poster]")).map(ele -> ele.attr("poster"));
		}

		/**
		 * 获取第一张图片或者视频封面地址
		 * 
		 * @return
		 */
		public Optional<String> getFirstImage() {
			return JsoupUtils.getFirstImage(document);
		}

		public Document getDocument() {
			return document;
		}
	}

}
