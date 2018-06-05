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

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.StringUtils;

public class Menu {

	private final Message name;
	private final String path;
	private final String id;

	private List<Menu> children = new ArrayList<>();

	public Menu(Message name, String path) {
		super();
		this.name = name;
		this.path = path;
		this.id = StringUtils.uuid();
	}

	public Menu(Message name) {
		this(name, null);
	}

	public Message getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}

	public Menu addChild(Menu child) {
		children.add(child);
		return this;
	}

	public String getId() {
		return id;
	}

}
