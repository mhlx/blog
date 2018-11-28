package me.qyh.blog.template.render;

public final class DefaultGravatarUrlGenerator implements GravatarUrlGenerator {

	private final String urlPrefix;

	public DefaultGravatarUrlGenerator(String urlPrefix) {
		super();
		String finalPrefix = urlPrefix;
		if (!finalPrefix.endsWith("/")) {
			finalPrefix += "/";
		}
		this.urlPrefix = finalPrefix;
	}

	@Override
	public String getUrl(String md5) {
		return urlPrefix + md5;
	}
}
