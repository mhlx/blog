package me.qyh.blog.template.render;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.template.PreviewTemplate;
import me.qyh.blog.template.entity.Fragment;

public class Fragments {

	private Fragments() {
		super();
	}

	public static String getCurrentTemplateName(String name) {
		String templateName = Fragment.getTemplateName(name, Environment.getSpace());
		if (Environment.isPreview()) {
			return PreviewTemplate.getTemplateName(templateName);
		}
		return templateName;
	}

}
