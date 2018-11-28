package me.qyh.blog.core.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.util.CollectionUtils;

/**
 * 用来将markdown文本转化为html文本 <br>
 * {@link https://github.com/atlassian/commonmark-java}
 * 
 * @author Administrator
 *
 */
public class CommonMarkdown2Html implements Markdown2Html {

	private final Parser parser;
	private final HtmlRenderer renderer;

	private static final List<Extension> BASE_EXTENSIONS = List.of(AutolinkExtension.create(), TablesExtension.create(),
			StrikethroughExtension.create(), HeadingAnchorExtension.create());

	public static final CommonMarkdown2Html INSTANCE = new CommonMarkdown2Html();

	private CommonMarkdown2Html() {
		this(Collections.emptyList());
	}

	public CommonMarkdown2Html(List<Extension> extensions) {
		List<Extension> baseExtensions = new ArrayList<>(BASE_EXTENSIONS);
		if (!CollectionUtils.isEmpty(extensions)) {
			baseExtensions.addAll(extensions);
		}
		parser = Parser.builder().extensions(baseExtensions).build();
		renderer = HtmlRenderer.builder().extensions(baseExtensions).build();
	}

	@Override
	public String toHtml(String markdown) {
		if (markdown == null) {
			return "";
		}
		Node document = parser.parse(markdown);
		return renderer.render(document);
	}

}
