package me.qyh.blog.template.vo;

public class BackendTemplateInfo {

	private final String path;
	private final boolean rewrite;

	public BackendTemplateInfo(String path, boolean rewrite) {
		super();
		this.path = path;
		this.rewrite = rewrite;
	}

	public String getPath() {
		return path;
	}

	public boolean isRewrite() {
		return rewrite;
	}

}
