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
package me.qyh.blog.template.vo;

import java.util.Map;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

public class DataTag {

	private String name;
	private final Map<String, Object> attrs;

	public String getName() {
		return name;
	}

	public DataTag(String name, Map<String, Object> attrs) {
		this.name = name;
		if (attrs == null) {
			this.attrs = Map.of();
		} else {
			this.attrs = attrs;
		}
	}

	public Map<String, Object> getAttrs() {
		return attrs;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void put(String key, String v) {
		attrs.put(key, v);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attrs, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			DataTag other = (DataTag) obj;
			return Objects.equals(this.attrs, other.attrs) && Objects.equals(this.name, other.name);
		}
		return false;
	}

	public boolean hasKey(String key) {
		return attrs.containsKey(key);
	}

	@Override
	public String toString() {
		return "DataTag [name=" + name + ", attrs=" + attrs + "]";
	}

}
