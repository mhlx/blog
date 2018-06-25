package me.qyh.blog.template;

import java.util.Optional;

import me.qyh.blog.core.util.FileUtils;

/**
 * 管理台页面
 * 
 * @since 6.5
 * @author wwwqyhme
 *
 */
public abstract class BackendTemplate implements Template {

	private final String path;
	private static final String BACKEND_PREFIX = TEMPLATE_PREFIX + "Backend" + SPLITER;

	public BackendTemplate(String path) {
		super();
		this.path = FileUtils.cleanPath(path);
	}

	@Override
	public String getTemplateName() {
		return BACKEND_PREFIX + path;
	}

	@Override
	public boolean isCallable() {
		return false;
	}

	public String getPath() {
		return path;
	}

	public static boolean isBackendTemplate(String templateName) {
		return templateName != null && templateName.startsWith(BACKEND_PREFIX);
	}

	public static Optional<String> getPath(String templateName) {
		return isBackendTemplate(templateName) ? Optional.of(templateName.substring(BACKEND_PREFIX.length()))
				: Optional.empty();
	}

	public static String getTemplateName(String path) {
		return BACKEND_PREFIX + path;
	}

}