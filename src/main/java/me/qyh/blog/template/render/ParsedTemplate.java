package me.qyh.blog.template.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * 模板的解析顺序
 * 
 * @since 6.5
 * @author wwwqyhme
 *
 */
public class ParsedTemplate {
	private final String templateName;
	private final boolean preview;
	private final boolean callable;
	private final List<ParsedTemplate> children = new ArrayList<>();

	public ParsedTemplate(String templateName, boolean preview, boolean callable) {
		super();
		this.templateName = templateName;
		this.preview = preview;
		this.callable = callable;
	}

	ParsedTemplate(ParsedTemplate source) {
		this.templateName = source.templateName;
		this.preview = source.preview;
		this.callable = source.callable;
	}

	public String getTemplateName() {
		return templateName;
	}

	public boolean isPreview() {
		return preview;
	}

	public boolean isCallable() {
		return callable;
	}

	public void addChild(ParsedTemplate parsedTemplate) {
		this.children.add(parsedTemplate);
	}

	public List<ParsedTemplate> getChildren() {
		return this.children;
	}

	public Optional<ParsedTemplate> getChild(String templateName) {
		for (ParsedTemplate child : children) {
			if (child.getTemplateName().equals(templateName)) {
				return Optional.of(child);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(templateName).append(System.lineSeparator());
		append(sb, this, 0);
		return sb.toString();
	}

	private void append(StringBuilder sb, ParsedTemplate root, int indent) {
		int nextIndent = indent += 2;
		for (ParsedTemplate child : root.getChildren()) {
			IntStream.range(0, indent).forEach(i -> sb.append(" "));
			sb.append(child.getTemplateName()).append(System.lineSeparator());
			append(sb, child, nextIndent);
		}
	}
}