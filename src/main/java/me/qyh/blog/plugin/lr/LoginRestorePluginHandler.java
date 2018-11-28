package me.qyh.blog.plugin.lr;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.AttemptLoggerManager;
import me.qyh.blog.core.security.GoogleAuthenticator;

public class LoginRestorePluginHandler extends PluginHandlerSupport {

	private static final String ENABLE_KEY = "plugin.lr.enable";
	private static final String ATTEMPT_COUNT_KEY = "plugin.lr.attempt";
	private static final String MAX_ATTEMPT_COUNT_KEY = "plugin.lr.maxattempt";
	private static final String ATTEMPT_SEC_KEY = "plugin.lr.attemptsec";

	private GoogleAuthenticator ga;
	private AttemptLoggerManager attemptLoggerManager;

	private final PluginProperties properties = PluginProperties.getInstance();

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		this.ga = getBean(GoogleAuthenticator.class, applicationContext).orElse(null);
		this.attemptLoggerManager = applicationContext.getBean(AttemptLoggerManager.class);
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		if (ga != null) {

			Map<String, String> proMap = properties.gets(ATTEMPT_COUNT_KEY, MAX_ATTEMPT_COUNT_KEY, ATTEMPT_SEC_KEY);

			int attemptCount = proMap.containsKey(ATTEMPT_COUNT_KEY) ? Integer.parseInt(proMap.get(ATTEMPT_COUNT_KEY))
					: 5;
			int maxAttemptCount = proMap.containsKey(MAX_ATTEMPT_COUNT_KEY)
					? Integer.parseInt(proMap.get(MAX_ATTEMPT_COUNT_KEY))
					: 10;
			int sec = proMap.containsKey(ATTEMPT_SEC_KEY) ? Integer.parseInt(proMap.get(ATTEMPT_SEC_KEY)) : 3600;

			AttemptLogger attemptLogger = attemptLoggerManager.createAttemptLogger(attemptCount, maxAttemptCount, sec);

			registry.register(LoginRestoreController.class.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(LoginRestoreController.class)
							.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(attemptLogger)
							.getBeanDefinition());

		}
	}

	@Override
	public boolean enable() {
		return properties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(true);
	}

}
