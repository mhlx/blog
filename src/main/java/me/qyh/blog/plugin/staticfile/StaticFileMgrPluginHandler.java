package me.qyh.blog.plugin.staticfile;

import java.util.Optional;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.Icon;
import me.qyh.blog.core.plugin.IconRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class StaticFileMgrPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pros = PluginProperties.getInstance();

	private static final String ENABLE_KEY = "plugin.staticfilemgr.enable";
	private static final String LOCATION_KEY = "plugin.staticfilemgr.location";

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.scanAndRegister("validator");
		Optional<String> opLocation = pros.get(LOCATION_KEY);
		String location = opLocation.get();
		registry.register(StaticFileManager.class.getName(),
				BeanDefinitionBuilder.genericBeanDefinition(StaticFileManager.class).addConstructorArgValue(location)
						.setScope(BeanDefinition.SCOPE_SINGLETON).getBeanDefinition());
		registry.register(StaticFileConsole.class.getName(), simpleBeanDefinition(StaticFileConsole.class));
		registry.register(StaticFileMgrController.class.getName(), simpleBeanDefinition(StaticFileMgrController.class));
	}

	@Override
	public void addIcon(IconRegistry registry) {
		registry.addIcon(new Icon(new Message("plugin.staticFile.iconName", "静态文件"), "<i class=\"fas fa-file\"></i>",
				"console/staticFile"));
	}

	@Override
	public boolean enable() {
		if (pros.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(true)) {
			Optional<String> opLocation = pros.get(LOCATION_KEY);
			if (opLocation.isPresent()) {
				return !opLocation.get().trim().isEmpty();
			}
		}
		return false;
	}

}
