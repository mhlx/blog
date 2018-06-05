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

import java.lang.reflect.Type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.util.HtmlUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import me.qyh.blog.core.config.Constants;

/**
 * 对对象的string类型属性进行标签转化
 * 
 * @author mhlx
 *
 */
public class MessageSerializer implements JsonSerializer<Message> {

	@Autowired
	private Messages messages;

	@Override
	public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
		if (messages == null) {
			// 如果没有注入messages,那么尝试注入
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		}
		// 如果不是在spring环境中，那么尝试使用code来输出
		String msg = messages == null ? src.getCodes()[0]
				: HtmlUtils.htmlEscape(messages.getMessage(src), Constants.CHARSET.name());
		return new JsonPrimitive(msg);
	}

}
