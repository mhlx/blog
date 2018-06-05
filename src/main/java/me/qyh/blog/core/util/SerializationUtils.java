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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import me.qyh.blog.core.exception.SystemException;


public final class SerializationUtils {

	private SerializationUtils() {

	}

	/**
	 * 序列化一个对象
	 * 
	 * @param obj
	 *            本身或者其实现类必须实现<code>java.io.Serializable</code>接口
	 * @param os
	 *            流，不会关闭！！！
	 */
	public static void serialize(Object obj, OutputStream os) throws IOException {
		enableSerializable(obj);
		ObjectOutputStream out = new ObjectOutputStream(os);
		out.writeObject(obj);
	}

	/**
	 * 序列化一个对象
	 * 
	 * @param obj
	 *            本身或者其实现类必须实现<code>java.io.Serializable</code>接口
	 * @return 字节
	 */
	public static byte[] serialize(Object obj) throws IOException {
		enableSerializable(obj);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(obj);
			return baos.toByteArray();
		}
	}

	/**
	 * 反序列化
	 * 
	 * @param is
	 *            流，不会关闭！！！
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(InputStream is) throws IOException {
		ObjectInputStream in = new ObjectInputStream(is);
		try {
			return (T) in.readObject();
		} catch (ClassNotFoundException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	/**
	 * 反序列化
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] bits) throws IOException {
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bits))) {
			try {
				return (T) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new SystemException(e.getMessage(), e);
			}
		}
	}

	/**
	 * 将对象序列化后写入文件
	 * 
	 * @param obj
	 *            本身或者其实现类必须实现<code>java.io.Serializable</code>接口
	 * @param path
	 *            文件，如果不存在，则会被创建
	 * @throws IOException
	 */
	public static void serialize(Object obj, Path path) throws IOException {
		try (OutputStream os = Files.newOutputStream(path)) {
			serialize(obj, os);
		}
	}

	/**
	 * 反序列化
	 * 
	 * @param path
	 *            文件
	 * @return
	 * @throws IOException
	 */
	public static <T> T deserialize(Path path) throws IOException {
		try (InputStream is = Files.newInputStream(path)) {
			return deserialize(is);
		}
	}

	private static void enableSerializable(Object obj) {
		if (!(obj instanceof Serializable)) {
			throw new SystemException(obj + "没有实现Serializable接口");
		}
	}
}
