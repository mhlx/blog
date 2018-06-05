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
package me.qyh.blog.plugin.qiniu;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.plugin.FileStoreRegistry;
import me.qyh.blog.core.plugin.PluginHandler;
import me.qyh.blog.core.plugin.PluginProperties;

public class QiniuPluginHandler implements PluginHandler {

	private PluginProperties pluginProperties = PluginProperties.getInstance();

	private static final String ENABLE_KEY = "plugin.qiniu.enable";
	private static final String ACCESSKEY_KEY = "plugin.qiniu.accesskey";
	private static final String SECRETKEY_KEY = "plugin.qiniu.secretkey";
	private static final String BUCKET_KEY = "plugin.qiniu.bucket";
	private static final String LARGE_SIZE_KEY = "plugin.qiniu.large";
	private static final String MIDDLE_SIZE_KEY = "plugin.qiniu.middle";
	private static final String SMALL_SIZE_KEY = "plugin.qiniu.small";
	private static final String READONLY_KEY = "plugin.qiniu.readonly";
	private static final String SOURCE_PROTECTED_KEY = "plugin.qiniu.sourceProtected";
	private static final String STYLE_KEY = "plugin.qiniu.style";
	private static final String STYLE_SPLIT_CHAR_KEY = "plugin.qiniu.styleSplitChar";
	private static final String URL_PREFIX_KEY = "plugin.qiniu.urlPrefix";
	private static final String ID_KEY = "plugin.qiniu.id";
	private static final String NAME_KEY = "plugin.qiniu.name";

	@Override
	public void addFileStore(FileStoreRegistry registry) {

		Integer id = pluginProperties.get(ID_KEY).map(Integer::parseInt)
				.orElseThrow(() -> new SystemException("请提供存储器ID"));
		String name = pluginProperties.get(NAME_KEY).orElse("七牛云存储");

		QiniuConfig config = new QiniuConfig();
		pluginProperties.get(ACCESSKEY_KEY).ifPresent(config::setAccessKey);
		pluginProperties.get(SECRETKEY_KEY).ifPresent(config::setSecretKey);
		pluginProperties.get(BUCKET_KEY).ifPresent(config::setBucket);
		pluginProperties.get(LARGE_SIZE_KEY).map(Integer::parseInt).ifPresent(config::setLargeSize);
		pluginProperties.get(MIDDLE_SIZE_KEY).map(Integer::parseInt).ifPresent(config::setMiddleSize);
		pluginProperties.get(SMALL_SIZE_KEY).map(Integer::parseInt).ifPresent(config::setSmallSize);
		pluginProperties.get(READONLY_KEY).map(Boolean::parseBoolean).ifPresent(config::setReadonly);
		pluginProperties.get(SOURCE_PROTECTED_KEY).map(Boolean::parseBoolean).ifPresent(config::setSourceProtected);
		pluginProperties.get(STYLE_KEY).ifPresent(config::setStyle);
		pluginProperties.get(URL_PREFIX_KEY).ifPresent(config::setUrlPrefix);
		pluginProperties.get(STYLE_SPLIT_CHAR_KEY).ifPresent(splitChar -> {

			char[] charArray = splitChar.toCharArray();
			if (charArray.length > 1) {
				throw new SystemException(STYLE_SPLIT_CHAR_KEY + "应该只对应一个字符");
			}
			if (charArray.length > 0) {
				config.setStyleSplitChar(charArray[0]);
			}
		});

		QiniuFileStore store = new QiniuFileStore(id, name, config);
		registry.register(store);

	}

	@Override
	public boolean enable() {
		return pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);
	}

}
