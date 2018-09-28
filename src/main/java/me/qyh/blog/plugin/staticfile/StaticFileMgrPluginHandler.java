package me.qyh.blog.plugin.staticfile;

import java.util.Optional;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.Menu;
import me.qyh.blog.core.plugin.MenuRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class StaticFileMgrPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pros = PluginProperties.getInstance();

	private static final String ENABLE_KEY = "plugin.staticfilemgr.enable";
	private static final String LOCATION_KEY = "plugin.staticfilemgr.location";
	private static final String PREFIX_KEY = "plugin.staticfilemgr.prefix";

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.scanAndRegister("validator");
		Optional<String> opLocation = pros.get(LOCATION_KEY);
		Optional<String> opPrefix = pros.get(PREFIX_KEY);
		boolean hasHandler = false;
		if (opLocation.isPresent() && opPrefix.isPresent()) {
			String location = opLocation.get();
			String prefix = opPrefix.get();
			if (!location.strip().isEmpty() && !prefix.strip().isEmpty()) {
				registry.register(EditablePathResourceHttpRequestHandler.class.getName(),
						BeanDefinitionBuilder.genericBeanDefinition(EditablePathResourceHttpRequestHandler.class)
								.addConstructorArgValue(location).addConstructorArgValue(prefix)
								.setScope(BeanDefinition.SCOPE_SINGLETON).getBeanDefinition());
				hasHandler = true;
			}
		}
		if (!hasHandler) {
			registry.register(StaticResourceHttpRequestHandler.class.getName(),
					simpleBeanDefinition(StaticResourceHttpRequestHandler.class));
		}
		registry.register(StaticFileMgrController.class.getName(), simpleBeanDefinition(StaticFileMgrController.class));
	}

	@Override
	public void addMenu(MenuRegistry registry) throws Exception {
		registry.addMenu(new Menu(new Message("plugin.staticfilemgr.menu.index", "静态资源管理"), "mgr/static/index"));
	}

	@Override
	public boolean enable() {
		return pros.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(true);
	}

}
