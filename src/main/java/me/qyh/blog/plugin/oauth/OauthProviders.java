package me.qyh.blog.plugin.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import me.qyh.blog.core.exception.SystemException;

public class OauthProviders {

	private OauthProviders() {
		super();
	}

	private static final OauthProviders INS = new OauthProviders();

	static OauthProviders getProviders() {
		return INS;
	}

	boolean isEmpty() {
		return providers.isEmpty();
	}

	private List<OauthProvider> providers = new ArrayList<>();

	public Optional<OauthProvider> getOauthProvider(String name) {
		return providers.stream().filter(o -> Objects.equals(name, o.getName())).findAny();
	}

	void add(OauthProvider provider) {
		if (getOauthProvider(provider.getName()).isPresent()) {
			throw new SystemException("已经存在名为：" + provider.getName() + "的登录处理器了");
		}
		providers.add(provider);
	}
}
