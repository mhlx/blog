package me.qyh.blog.web.view;

public class TemplateView {

	private final String templateName;
	private final String matchPattern;

	public TemplateView(String templateName, String matchPattern) {
		super();
		this.templateName = templateName;
		this.matchPattern = matchPattern;
	}

	public String getTemplateName() {
		return templateName;
	}

	public String getMatchPattern() {
		return matchPattern;
	}

}
