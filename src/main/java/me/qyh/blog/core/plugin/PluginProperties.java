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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;

public class PluginProperties {

	private final EncodedResource proResource;

	private final Map<String, String> proMap = new HashMap<>();

	private final StampedLock lock = new StampedLock();

	private final Properties pros;

	private static final PluginProperties instance = new PluginProperties();

	private PluginProperties() {
		Path path = FileUtils.HOME_DIR.resolve("blog/plugin.properties");
		if (!FileUtils.exists(path)) {
			FileUtils.createFile(path);
		}
		proResource = new EncodedResource(new PathResource(path), Constants.CHARSET);
		try {
			pros = PropertiesLoaderUtils.loadProperties(proResource);
			doConvert();
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	public void remove(String... keys) {
		if (keys != null && keys.length > 0) {
			long stamp = lock.writeLock();
			try {
				Map<Object, Object> old = new HashMap<>();
				for (String key : keys) {
					Object oldV = pros.remove(key);
					if (oldV != null) {
						old.put(key, oldV);
					}
				}
				try (OutputStream os = new FileOutputStream(proResource.getResource().getFile())) {
					pros.store(os, "");
				} catch (IOException e) {
					old.forEach(pros::put);
					throw new SystemException(e.getMessage(), e);
				}
				doConvert();
			} finally {
				lock.unlockWrite(stamp);
			}
		}
	}

	public void store(Map<String, String> map) {
		long stamp = lock.writeLock();
		try {
			Map<Object, Object> old = new HashMap<>();
			Set<String> newKeys = new HashSet<>();
			for (Map.Entry<String, String> it : map.entrySet()) {
				String key = it.getKey();

				Object oldV = pros.put(key, it.getValue());
				if (oldV != null) {
					old.put(key, oldV);
				} else {
					newKeys.add(key);
				}
			}

			try (OutputStream os = new FileOutputStream(proResource.getResource().getFile())) {
				pros.store(os, "");
			} catch (IOException e) {
				old.forEach(pros::put);
				newKeys.forEach(pros::remove);
				throw new SystemException(e.getMessage(), e);
			}
			doConvert();

		} finally {
			lock.unlockWrite(stamp);
		}
	}

	public Optional<String> get(String key) {
		long stamp = lock.tryOptimisticRead();
		String value = doGet(key);
		if (!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				value = doGet(key);
			} finally {
				lock.unlockRead(stamp);
			}
		}
		return Optional.ofNullable(value);
	}

	public Map<String, String> gets(String... keys) {
		if (Validators.isEmpty(keys)) {
			return new HashMap<>();
		}
		long stamp = lock.tryOptimisticRead();
		Map<String, String> value = doGets(keys);
		if (!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				value = doGets(keys);
			} finally {
				lock.unlockRead(stamp);
			}
		}
		return value;
	}

	private Map<String, String> doGets(String... keys) {
		Map<String, String> map = new HashMap<>();
		for (String key : keys) {
			String v = proMap.get(key);
			if (v != null) {
				map.put(key, v);
			}
		}
		return map;
	}

	private String doGet(String key) {
		return proMap.get(key);
	}

	private void doConvert() {
		proMap.clear();
		pros.forEach((k, v) -> proMap.put(Objects.toString(k, ""), Objects.toString(v, "")));
	}

	public static PluginProperties getInstance() {
		return instance;
	}
}
