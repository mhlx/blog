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
package me.qyh.blog.core.util;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * JsonFormat annotation可以解决同样的问题，但只能局限一种格式？
 * 
 * @author Administrator
 *
 */
public class DateDeserializer implements JsonDeserializer<Timestamp> {

	@Override
	public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return toTimestamp(json.getAsString().trim());
	}

	private Timestamp toTimestamp(String str) {
		LocalDateTime localDateTime = Times.parse(str)
				.orElseThrow(() -> new DateParseProcessingException(str + "无法转化为符合格式的日期"));
		return Timestamp.valueOf(localDateTime);
	}

	private final class DateParseProcessingException extends JsonParseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected DateParseProcessingException(String msg) {
			super(msg);
		}
	}
}
