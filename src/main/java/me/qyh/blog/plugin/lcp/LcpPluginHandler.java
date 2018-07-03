package me.qyh.blog.plugin.lcp;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

/**
 * 注销后清空预览
 * 
 * @author wwwqyhme
 *
 */
public class LcpPluginHandler extends PluginHandlerSupport {

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.register(LcpSessionListener.class.getName(), simpleBeanDefinition(LcpSessionListener.class));
	}

	@Override
	public void initChild(ApplicationContext applicationContext) throws Exception {
		WebApplicationContext wac = (WebApplicationContext) applicationContext;
		wac.getServletContext().addListener(applicationContext.getBean(LcpSessionListener.class));
	}

	@Override
	public boolean enable() {
		return pluginProperties.get("plugin.lcp.enable").map(Boolean::parseBoolean).orElse(true);
	}

}
