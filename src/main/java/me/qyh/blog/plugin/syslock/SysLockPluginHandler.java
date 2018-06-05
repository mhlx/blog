/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.plugin.syslock;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.LockProviderRegistry;
import me.qyh.blog.core.plugin.Menu;
import me.qyh.blog.core.plugin.MenuRegistry;
import me.qyh.blog.core.plugin.MybatisConfigurer;
import me.qyh.blog.core.plugin.PluginHandlerRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.TemplateRegistry;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.plugin.syslock.component.SysLockProvider;

public class SysLockPluginHandler extends PluginHandlerSupport {

	private SysLockProvider provider;

	private final String rootPackage = PluginHandlerRegistry.getRootPluginPackage(this.getClass()) + ".";

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		provider = applicationContext.getBean(SysLockProvider.class);
	}

	@Override
	protected void registerBean(BeanRegistry registry) {
		registry.scanAndRegister(rootPackage + "component", rootPackage + "validator");
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		registry.scanAndRegister(rootPackage + "web.controller");
	}

	@Override
	public void configureMybatis(MybatisConfigurer configurer) throws Exception {
		String rootPath = rootPackage.replace('.', '/') + "mapper/";
		configurer.addBasePackages(rootPackage + "dao");
		configurer.addMapperLocations(new ClassPathResource(rootPath + "lockMapper.xml"));
		configurer.addTypeAliasResources(new ClassPathResource(rootPath + "typeAlias.txt"));

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
	public void addMenu(MenuRegistry registry) {
		registry.addMenu(new Menu(new Message("plugin.syslock.menu.mgr", "系统锁管理"), "mgr/lock/sys/index"));
	}

}
