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
package me.qyh.blog.template.render.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.vo.DataBind;

public abstract class DataTagProcessor<T> {

	private String name;// 数据名，唯一
	private String dataName;// 默认数据绑定名，唯一
	private boolean callable;// 是否可以被ajax调用

	protected static final Logger LOGGER = LoggerFactory.getLogger(DataTagProcessor.class);

	/**
	 * 构造器
	 * 
	 * @param name
	 *            数据处理器名称
	 * @param dataName
	 *            页面dataName
	 */
	public DataTagProcessor(String name, String dataName) {
		this.name = name;
		this.dataName = dataName;
	}

	/**
	 * 查询数据
	 * 
	 * @param variables
	 * @param attributes
	 * @return
	 * @throws LogicException
	 */
	public final DataBind getData(Map<String, Object> attributes) throws LogicException {
		Attributes atts = new Attributes(attributes);
		T result = query(atts);
		DataBind bind = new DataBind();
		bind.setData(result);
		bind.setDataName(dataName);
		return bind;
	}

	protected abstract T query(Attributes attributes) throws LogicException;

	public String getName() {
		return name;
	}

	public String getDataName() {
		return dataName;
	}

	public boolean isCallable() {
		return callable;
	}

	public void setCallable(boolean callable) {
		this.callable = callable;
	}

	protected Space getCurrentSpace() {
		return Environment.getSpace();
	}

	public static boolean validDataName(String dataName) {
		if (Validators.isLetterOrNum(dataName)) {
			char first = dataName.charAt(0);
			return !Validators.isNum(first);
		}
		return false;
	}

	/**
	 * 获取data标签所有可能的属性
	 * 
	 * @return
	 */
	public abstract List<String> getAttributes();

	public final class Attributes {
		private final Map<String, Object> attMap;

		private Attributes(Map<String, Object> attMap) {
			this.attMap = attMap == null ? new HashMap<>() : attMap;
		}

		public Optional<Object> get(String key) {
			return Optional.ofNullable(attMap.get(key));
		}

		public Optional<String> getString(String key) {
			return get(key).map(Object::toString);
		}

		/**
		 * 将某个属性转化为Enum
		 * 
		 * @param name
		 *            属性名
		 * @param e
		 *            如果转化失败或者属性不存在，返回null
		 * @param upperCase
		 *            是否转化大写
		 * @return
		 */
		public <E extends Enum<E>> Optional<E> getEnum(String name, Class<E> e) {
			try {
				return getString(name).map(String::toUpperCase).map(attV -> Enum.valueOf(e, attV));
			} catch (IllegalArgumentException ex) {
				return Optional.empty();
			}
		}

		/**
		 * 将某个属性转化为boolean
		 * 
		 * @param name
		 *            属性名
		 * @param defaultValue
		 *            默认值 如果属性不存在，返回默认值
		 * @return 如果属性不存在，返回null
		 */
		public Optional<Boolean> getBoolean(String name) {
			return getString(name).map(Boolean::parseBoolean);
		}

		/**
		 * 将某个属性转化为Integer
		 * 
		 * @param name
		 *            属性名
		 * @param defaultValue
		 *            如果属性不存在，返回默认值
		 * @return 如果转化失败或者不存在该属性，返回null
		 * 
		 * @see NumberFormatException
		 */
		public Optional<Integer> getInteger(String name) {
			try {
				return getString(name).map(Integer::parseInt);
			} catch (NumberFormatException e) {
				return Optional.empty();
			}
		}

		/**
		 * 将某个属性转化为Set
		 * 
		 * @param name
		 *            属性名
		 * @param split
		 *            分割字符
		 * @return 如果属性不存在，返回空Set，不返回null
		 */
		public Set<String> getSet(String name, String split) {
			return new HashSet<>(getList(name, split));
		}

		/**
		 * 将某个属性转化为List
		 * 
		 * @param name
		 *            属性名
		 * @param split
		 *            分割字符
		 * @return 如果属性不存在，返回空Set，不返回null
		 */
		public List<String> getList(String name, String split) {
			String[] array = getArray(name, split);
			if (array == null) {
				return new ArrayList<>();
			}
			List<String> list = new ArrayList<>(array.length);
			Collections.addAll(list, array);
			return list;
		}

		/**
		 * 将某个属性转化为Array
		 * 
		 * @param name
		 *            属性名
		 * @param split
		 *            分割字符
		 * @return 如果属性不存在，返回null
		 */
		public String[] getArray(String name, String split) {
			return getString(name).map(v -> v.split(split)).orElse(null);
		}

		@Override
		public String toString() {
			return "Attributes [attMap=" + attMap + "]";
		}
	}
}
