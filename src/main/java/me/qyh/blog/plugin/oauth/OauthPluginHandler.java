package me.qyh.blog.plugin.oauth;

import org.springframework.context.ConfigurableApplicationContext;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class OauthPluginHandler extends PluginHandlerSupport {

	private static final String GITHUB_ENABLE_KEY = "plugin.oauth.github.enable";
	private static final String GITHUB_CLIENT_ID_KEY = "plugin.oauth.github.clientId";
	private static final String GITHUB_CLIENT_SECRET_KEY = "plugin.oauth.github.clientSecret";
	private static final String GITHUB_ACCOUNT_KEY = "plugin.oauth.github.account";

	private static final String GITLAB_ENABLE_KEY = "plugin.oauth.gitlab.enable";
	private static final String GITLAB_CLIENT_ID_KEY = "plugin.oauth.gitlab.clientId";
	private static final String GITLAB_CLIENT_SECRET_KEY = "plugin.oauth.gitlab.clientSecret";
	private static final String GITLAB_ACCOUNT_KEY = "plugin.oauth.gitlab.account";

	@Override
	public void initializeOther(ConfigurableApplicationContext applicationContext) {
		PluginProperties pp = PluginProperties.getInstance();

		if (pp.get(GITHUB_ENABLE_KEY).map(Boolean::parseBoolean).orElse(false)) {
			String clientId = pp.get(GITHUB_CLIENT_ID_KEY).orElseThrow();
			String clientSecret = pp.get(GITHUB_CLIENT_SECRET_KEY).orElseThrow();
			String account = pp.get(GITHUB_ACCOUNT_KEY).orElseThrow();

			GithubOauthProvider provider = new GithubOauthProvider(clientId, clientSecret, account);
			OauthProviders.getProviders().add(provider);
		}

		if (pp.get(GITLAB_ENABLE_KEY).map(Boolean::parseBoolean).orElse(false)) {
			String clientId = pp.get(GITLAB_CLIENT_ID_KEY).orElseThrow();
			String clientSecret = pp.get(GITLAB_CLIENT_SECRET_KEY).orElseThrow();
			String account = pp.get(GITLAB_ACCOUNT_KEY).orElseThrow();

			GitlabOauthProvider provider = new GitlabOauthProvider(clientId, clientSecret, account);
			OauthProviders.getProviders().add(provider);
		}
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		if (!OauthProviders.getProviders().isEmpty()) {
			registry.register(OauthController.class.getName(), simpleBeanDefinition(OauthController.class));
		}
	}

}
