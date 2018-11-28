package me.qyh.blog.template;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.Validators;

/**
 * 系统内置模板
 * 
 * @author mhlx
 *
 */
public final class SystemTemplate implements PathTemplate {

	private static final String SYSTEM_PREFIX = TEMPLATE_PREFIX + "System" + SPLITER;
	private final String path;
	private String template;
	private String templateName;

	private SystemTemplate(SystemTemplate systemTemplate) {
		this.path = systemTemplate.path;
		this.template = systemTemplate.template;
	}

	public SystemTemplate(String path, String template) {
		this.path = path;
		this.template = template;
	}

	public SystemTemplate(String path, Resource resource) {
		super();
		this.path = path;
		try {
			this.template = Resources.readResourceToString(resource);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public String getTemplateName() {
		if (templateName == null) {
			templateName = SYSTEM_PREFIX + FileUtils.cleanPath(path);
		}
		return templateName;
	}

	@Override
	public SystemTemplate cloneTemplate() {
		return new SystemTemplate(this);
	}

	@Override
	public boolean isCallable() {
		return false;
	}

	@Override
	public boolean equalsTo(Template other) {
		if (Validators.baseEquals(this, other)) {
			SystemTemplate rhs = (SystemTemplate) other;
			return Objects.equals(this.path, rhs.path);
		}
		return false;
	}

	public static boolean isSystemTemplate(String templateName) {
		return templateName != null && templateName.startsWith(SYSTEM_PREFIX);
	}

	public static Optional<String> getPath(String templateName) {
		return isSystemTemplate(templateName) ? Optional.of(templateName.substring(SYSTEM_PREFIX.length()))
				: Optional.empty();
	}

	@Override
	public String getRelativePath() {
		return path;
	}
}
