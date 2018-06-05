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
package me.qyh.blog.plugin.wechat;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;

public class WechatPluginHandler extends PluginHandlerSupport {

	private static final String APPID_KEY = "plugin.wechat.appid";
	private static final String APPSECRET_KEY = "plugin.wechat.appsecret";
	private static final String ENABLE_KEY = "plugin.wechat.enable";

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		Map<String, String> map = PluginProperties.getInstance().gets(APPID_KEY, APPSECRET_KEY);
		WechatSupport wechatSupport = new WechatSupport(map.get(APPID_KEY), map.get(APPSECRET_KEY));
		registry.register(WechatController.class.getName(),
				BeanDefinitionBuilder.genericBeanDefinition(WechatController.class)
						.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(wechatSupport)
						.getBeanDefinition());
	}

	@Override
	public boolean enable() {
		return PluginProperties.getInstance().get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);
	}
}
