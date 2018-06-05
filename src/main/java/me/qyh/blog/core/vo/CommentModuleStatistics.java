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
package me.qyh.blog.core.vo;

import me.qyh.blog.core.message.Message;

public class CommentModuleStatistics {

	private final String type;
	private final Message name;
	private final int count;

	public CommentModuleStatistics(String type, Message name, int count) {
		super();
		this.type = type;
		this.name = name;
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public Message getName() {
		return name;
	}

	public int getCount() {
		return count;
	}

}
