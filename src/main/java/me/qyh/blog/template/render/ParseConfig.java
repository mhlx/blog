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
package me.qyh.blog.template.render;

public class ParseConfig {
	private final boolean onlyCallable;

	/**
	 * @since 6.5
	 */
	private String contentType;

	public ParseConfig(boolean onlyCallable) {
		super();
		this.onlyCallable = onlyCallable;
	}

	public ParseConfig(boolean onlyCallable, String contentType) {
		super();
		this.onlyCallable = onlyCallable;
		this.contentType = contentType;
	}

	public ParseConfig() {
		this(false);
	}

	public boolean isOnlyCallable() {
		return onlyCallable;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}