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
package me.qyh.blog.core.exception;

import me.qyh.blog.core.message.Message;

/**
 * 业务异常，这个异常不做任何的日志
 * 
 * @author Administrator
 *
 */
public class LogicException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Message logicMessage;

	/**
	 * @param message
	 *            异常信息
	 */
	public LogicException(Message message) {
		super(null, null, false, false);
		this.logicMessage = message;
	}

	/**
	 * @param code
	 *            异常码
	 * @param defaultMessage
	 *            默认信息
	 * @param args
	 *            参数
	 */
	public LogicException(String code, String defaultMessage, Object... args) {
		this(new Message(code, defaultMessage, args));
	}

	/**
	 * 
	 * @param code
	 *            异常码
	 * @param args
	 *            参数
	 */
	public LogicException(String code, Object... args) {
		this(new Message(code, null, args));
	}

	public Message getLogicMessage() {
		return logicMessage;
	}
}
