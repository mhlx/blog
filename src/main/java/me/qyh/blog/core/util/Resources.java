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
package me.qyh.blog.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.config.Constants;

public final class Resources {

	private Resources() {
	}

	/**
	 * 读取Resource资源内容
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public static String readResourceToString(Resource resource) throws IOException {
		return applyResource(resource, Resources::read);
	}

	/**
	 * 读取resource内容
	 * 
	 * @param resource
	 * @param consumer
	 * @throws IOException
	 */
	public static void readResource(Resource resource, ResourceConsumer consumer) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			consumer.accept(is);
		}
	}

	/**
	 * 转化resource中的内容
	 * 
	 * @param resource
	 * @param fun
	 * @return
	 * @throws IOException
	 */
	public static <T> T applyResource(Resource resource, ResourceFunction<T> fun) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			return fun.apply(is);
		}
	}

	@FunctionalInterface
	public interface ResourceFunction<R> {
		R apply(InputStream is) throws IOException;
	}

	@FunctionalInterface
	public interface ResourceConsumer {
		void accept(InputStream is) throws IOException;
	}

	/**
	 * InputStream to string
	 * <p>
	 * <b>not close is!!!</b>
	 * </p>
	 * 
	 * @param is
	 * @return
	 */
	public static String read(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, Constants.CHARSET));
		return reader.lines().collect(Collectors.joining(System.lineSeparator()));
	}
}
