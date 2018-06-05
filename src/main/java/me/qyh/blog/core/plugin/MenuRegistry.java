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
import java.util.Collections;
import java.util.List;

public class MenuRegistry {

	private static final MenuRegistry REGISTRY = new MenuRegistry();

	private List<Menu> menus = new ArrayList<>();

	private MenuRegistry() {
		super();
	}

	public MenuRegistry addMenu(Menu menu) {
		menus.add(menu);
		return this;
	}

	public List<Menu> getMenus() {
		return Collections.unmodifiableList(menus);
	}

	public static MenuRegistry getInstance() {
		return REGISTRY;
	}

}
