package me.qyh.blog.plugin.syslock;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.Icon;
import me.qyh.blog.core.plugin.IconRegistry;
import me.qyh.blog.core.plugin.LockProviderRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.TemplateRegistry;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.plugin.syslock.component.SysLockProvider;

public class SysLockPluginHandler extends PluginHandlerSupport {

	private SysLockProvider provider;

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		provider = applicationContext.getBean(SysLockProvider.class);
	}

	@Override
	protected void registerBean(BeanRegistry registry) {
		registry.scanAndRegister("component", "validator");
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.scanAndRegister("web.controller");
	}

	@Override
	public void configureMybatis(RelativeMybatisConfigurer configurer) throws Exception {
		configurer.addBasePackages("dao");
		configurer.addRelativeMapperLocationPattern("mapper/*.xml");
		configurer.addRelativeTypeAliasLocations("mapper/typeAlias.txt");

	}

	@Override
	public void addTemplate(TemplateRegistry registry) throws Exception {
		String qaTemplate = Resources
				.readResourceToString(new ClassPathResource("me/qyh/blog/plugin/syslock/template/qa.html"));
		String pwdTemplate = Resources
				.readResourceToString(new ClassPathResource("me/qyh/blog/plugin/syslock/template/password.html"));
		registry.registerSystemTemplate("unlock/qa", qaTemplate);
		registry.registerSystemTemplate("space/{alias}/unlock/qa", qaTemplate);

		registry.registerSystemTemplate("unlock/password", pwdTemplate);
		registry.registerSystemTemplate("space/{alias}/unlock/password", pwdTemplate);
	}

	@Override
	public void addLockProvider(LockProviderRegistry registry) {
		registry.register(provider);
	}

	@Override
	public void addIcon(IconRegistry registry) {
		registry.addIcon(new Icon(new Message("plugin.sysLock.iconName", "系统锁"), "<i class=\"fas fa-lock\"></i>",
				"console/syslock"));
	}
}
