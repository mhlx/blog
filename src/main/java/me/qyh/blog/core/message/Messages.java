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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 
 * @author Administrator
 *
 */
public class Messages {

	@Autowired
	private MessageSource messageSource;

	/**
	 * 从message对象获取信息
	 * 
	 * @param message
	 *            message
	 * @return 信息
	 */
	public String getMessage(Message message) {
		// to ignore NoSuchMessageException
		return messageSource.getMessage(message.getCodes()[0], message.getArguments(), message.getDefaultMessage(),
				LocaleContextHolder.getLocale());
	}

	/**
	 * 从错误码和默认信息中获取信息
	 * 
	 * @param code
	 *            错误码
	 * @param defaultMessage
	 *            默认信息
	 * @return 信心
	 */
	public String getMessage(String code, String defaultMessage, Object... args) {
		return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
	}

}
