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
package me.qyh.blog.core.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Validators;

public class PluginSqlSessionFactoryBean extends SqlSessionFactoryBean {

	public PluginSqlSessionFactoryBean(List<Resource> mapperLocations, List<Resource> typeAliasResources)
			throws IOException {
		if (!CollectionUtils.isEmpty(mapperLocations)) {
			super.setMapperLocations(mapperLocations.toArray(Resource[]::new));
		}
		if (!CollectionUtils.isEmpty(typeAliasResources)) {
			Set<Class<?>> classSet = new HashSet<>();
			for (Resource typeAliasResource : typeAliasResources) {
				try (InputStream is = typeAliasResource.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is, Constants.CHARSET))) {
					reader.lines().forEach(line -> {

						if (!Validators.isEmptyOrNull(line, true)) {
							try {
								classSet.add(Class.forName(line));
							} catch (ClassNotFoundException e) {
								throw new SystemException(e.getMessage(), e);
							}

						}

					});
				}
			}
			if (!classSet.isEmpty()) {
				super.setTypeAliases(classSet.toArray(Class[]::new));
			}
		}
	}

}
