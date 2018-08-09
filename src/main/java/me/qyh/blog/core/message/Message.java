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
package me.qyh.blog.core.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.context.MessageSourceResolvable;

import me.qyh.blog.core.util.Validators;

/**
 * 用于Json结果的返回
 * 
 * @author mhlx
 *
 */
public class Message implements MessageSourceResolvable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String code;
	private String[] arguments;
	private String defaultMessage;

	/**
	 * 
	 * @param code
	 *            错误码
	 * @param defaultMessage
	 *            默认信息
	 * @param arguments
	 *            参数
	 */
	public Message(String code, String defaultMessage, Object... arguments) {
		this.code = code;
		this.arguments = Validators.isEmpty(arguments) ? null
				: Arrays.stream(arguments).map(Objects::toString).toArray(String[]::new);
		this.defaultMessage = defaultMessage;
	}

	/**
	 * @param code
	 *            错误码
	 */
	public Message(String code) {
		this.code = code;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	@Override
	public String[] getCodes() {
		return new String[] { code };
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return "Message [code=" + code + ", arguments=" + Arrays.toString(arguments) + ", defaultMessage="
				+ defaultMessage + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			Message rhs = (Message) obj;
			return Objects.equals(code, rhs.code);
		}
		return false;
	}
}
