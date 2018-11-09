/*
 * Copyright 2018 qyh.me
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
package me.qyh.blog.template;

public final class PatternAlreadyExistsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String pattern;
	private final String matchPattern;
	private final boolean keyPath;

	public PatternAlreadyExistsException(String pattern, String matchPattern) {
		super(null, null, false, false);
		this.pattern = pattern;
		this.matchPattern = matchPattern;
		this.keyPath = false;
	}

	public PatternAlreadyExistsException(String pattern) {
		super(null, null, false, false);
		this.pattern = pattern;
		this.matchPattern = null;
		this.keyPath = true;
	}

	public String getPattern() {
		return pattern;
	}

	public String getMatchPattern() {
		return matchPattern;
	}

	public boolean isKeyPath() {
		return keyPath;
	}
}