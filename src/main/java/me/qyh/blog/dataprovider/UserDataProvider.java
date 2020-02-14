package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.BlogConfigService;
import me.qyh.blog.vo.User;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class UserDataProvider extends DataProviderSupport<User> {

	private final BlogConfigService blogConfigService;

	public UserDataProvider(BlogConfigService blogConfigService) {
		super("user");
		this.blogConfigService = blogConfigService;
	}

	@Override
	public User provide(Map<String, String> attributesMap) throws Exception {
		return blogConfigService.getUser();
	}
}
