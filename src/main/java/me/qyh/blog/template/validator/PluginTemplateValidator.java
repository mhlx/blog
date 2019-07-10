package me.qyh.blog.template.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.entity.PluginTemplate;

@Component
public class PluginTemplateValidator implements Validator {

	private static final int MAX_NAME_LENGTH = 50;
	private static final int MAX_PLUGIN_NAME_LENGTH = 50;
	public static final int MAX_TEMPLATE_LENGTH = 200000;
	private static final String NAME_PATTERN = "^[A-Za-z0-9\u4E00-\u9FA5]+$";

	@Override
	public boolean supports(Class<?> clazz) {
		return PluginTemplate.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PluginTemplate template = (PluginTemplate) target;
		String pluginName = template.getPluginName();
		if (Validators.isEmptyOrNull(pluginName, true)) {
			errors.reject("pluginTemplate.pluginName.blank", "插件名为空");
			return;
		}

		if (pluginName.length() > MAX_PLUGIN_NAME_LENGTH) {
			errors.reject("pluginTemplate.pluginName.toolong", new Object[] { MAX_PLUGIN_NAME_LENGTH },
					"插件名长度不能超过" + MAX_PLUGIN_NAME_LENGTH + "个字符");
			return;
		}
		if (!pluginName.matches(NAME_PATTERN)) {
			errors.reject("pluginTemplate.pluginName.invalid", "无效的插件名");
			return;
		}

		String name = template.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("pluginTemplate.name.blank", "插件模板名为空");
		}
		if (name.length() > MAX_NAME_LENGTH) {
			errors.reject("pluginTemplate.name.toolong", new Object[] { MAX_NAME_LENGTH },
					"插件模板名长度不能超过" + MAX_NAME_LENGTH + "个字符");
			return;
		}
		if (!name.matches(NAME_PATTERN)) {
			errors.reject("pluginTemplate.name.invalid", "无效的插件模板名");
			return;
		}

		String tpl = template.getTemplate();
		if (Validators.isEmptyOrNull(tpl, true)) {
			errors.reject("pluginTemplate.template.null", "模板不能为空");
			return;
		}
		if (tpl.length() > MAX_TEMPLATE_LENGTH) {
			errors.reject("fragment.tpl.toolong", new Object[] { MAX_TEMPLATE_LENGTH },
					"模板长度不能超过" + MAX_TEMPLATE_LENGTH + "个字符");
			return;
		}
	}

}
