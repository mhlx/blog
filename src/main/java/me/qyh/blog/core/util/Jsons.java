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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.message.MessageSerializer;
import me.qyh.blog.core.vo.JsonResult;

/**
 * json处理工具类
 * 
 * @author Administrator
 *
 */
public class Jsons {

	private static final ExclusionStrategy SERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			Expose expose = f.getAnnotation(Expose.class);
			return expose != null && !expose.serialize();
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	};

	private static final ExclusionStrategy DESERIALIZATION_EXCLUSION_STRATEGY = new ExclusionStrategy() {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			Expose expose = f.getAnnotation(Expose.class);
			return expose != null && !expose.deserialize();
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	};

	private static final Gson gson = new GsonBuilder()
			.addSerializationExclusionStrategy(SERIALIZATION_EXCLUSION_STRATEGY)
			.addDeserializationExclusionStrategy(DESERIALIZATION_EXCLUSION_STRATEGY).enableComplexMapKeySerialization()
			.registerTypeAdapter(Message.class, new MessageSerializer())
			.registerTypeAdapter(Timestamp.class, new DateDeserializer())
			.registerTypeAdapter(Date.class, new DateSerializer()).setPrettyPrinting().create();

	private static final Logger LOGGER = LoggerFactory.getLogger(Jsons.class);
	private static final String SPLIT_STR = "->";
	private static final UrlReader DEFAULT_READER = new UrlResourceReader();

	private Jsons() {
		super();
	}

	/**
	 * for GsonHttpMessageConverter
	 * 
	 * @return
	 */
	public static Gson getGson() {
		return gson;
	}

	/**
	 * 将json tree转化为指定对象
	 * 
	 * @param t
	 *            目标类型
	 * @param json
	 *            jsontree
	 * @return
	 */
	public static <T> T readValue(Class<T> t, JsonElement json) {
		return gson.fromJson(json, t);
	}

	/**
	 * 将json字符串转化为目标对象
	 * 
	 * @param t
	 *            目标类型
	 * @param json
	 *            json字符串
	 * @return
	 */
	public static <T> T readValue(Class<T> t, String json) {
		return gson.fromJson(json, t);
	}

	/**
	 * 将json字符串转化为ArrayList集合
	 * 
	 * @param clazz
	 *            目标Array类型
	 * @param json
	 *            json字符串
	 * @return 可变的list
	 */
	public static <T> List<T> readList(Class<T[]> clazz, String json) {
		final T[] jsonToObject = gson.fromJson(json, clazz);
		return new ArrayList<>(Arrays.asList(jsonToObject));
	}

	/**
	 * 将对象输出为json文本
	 * 
	 * @param toWrite
	 *            对象
	 * @return
	 */
	public static String write(Object toWrite) {
		return gson.toJson(toWrite);
	}

	/**
	 * 将对象输出到writer
	 * 
	 * @param toWrite
	 *            对象
	 * @param writer
	 *            输出流
	 */
	public static void write(Object toWrite, Writer writer) {
		gson.toJson(toWrite, writer);
	}

	/**
	 * @see Jsons#read(String, UrlReader)
	 * @param url
	 * @return
	 */
	public static ExpressionExecutor read(String url) {
		return read(url, DEFAULT_READER);
	}

	/**
	 * @see Jsons#read(String, UrlReader)
	 * @param url
	 * @return
	 */
	public static ExpressionExecutors readForExecutors(String url) {
		return readForExecutors(url, DEFAULT_READER);
	}

	/**
	 * 读取连接中的内容(必须为json字符串)。通过表达式获取指定内容
	 * 
	 * @param url
	 *            url
	 * @param reader
	 *            表达式读取
	 * @return
	 */
	public static ExpressionExecutor read(String url, UrlReader reader) {
		try {
			return readJson(reader.read(url));
		} catch (IOException e) {
			return new ExpressionExecutor(JsonNull.INSTANCE, e);
		}
	}

	/**
	 * 读取连接中的内容(必须为json字符串)。通过表达式获取指定内容
	 * 
	 * @param url
	 *            url
	 * @param reader
	 *            表达式读取
	 * @return
	 */
	public static ExpressionExecutors readForExecutors(String url, UrlReader reader) {
		try {
			return readJsonForExecutors(reader.read(url));
		} catch (IOException e) {
			return new ExpressionExecutors(new JsonArray(0), e);
		}
	}

	/**
	 * 读取内容(必须为json字符串)。通过表达式获取指定内容
	 * 
	 * @param json
	 * @return
	 */
	public static ExpressionExecutor readJson(String json) {
		JsonElement je;
		try {
			JsonParser jp = new JsonParser();
			je = jp.parse(json);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			je = JsonNull.INSTANCE;
		}

		return new ExpressionExecutor(je);
	}

	/**
	 * 读取内容(必须为json字符串)。通过表达式获取指定内容
	 *
	 * @param json
	 * @return
	 */
	public static ExpressionExecutors readJsonForExecutors(String json) {
		JsonElement je;
		try {
			JsonParser jp = new JsonParser();
			je = jp.parse(json);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			je = JsonNull.INSTANCE;
		}

		return new ExpressionExecutors(toJsonArray(je));
	}

	/**
	 * 
	 * @param success
	 * @param data
	 * @return
	 */
	public static String toJsonResult(boolean success, Object data) {
		return gson.toJson(new JsonResult(success, data));
	}

	/**
	 * 
	 * @author mhlx
	 *
	 */
	@FunctionalInterface
	public interface UrlReader {

		/**
		 * 读取Url中的内容
		 * 
		 * @param url
		 * @exception IOException
		 * @return
		 */
		String read(String url) throws IOException;

	}

	private static final class UrlResourceReader implements UrlReader {

		@Override
		public String read(String url) throws IOException {
			UrlResource urlResource = new UrlResource(new URL(url));
			return Resources.readResourceToString(urlResource);
		}

	}

	/**
	 * <p>
	 * 假如json内容为
	 * 
	 * <pre>
	{
	"success": true,
	"data": {
	"data": {
	  "files": [
	    {
	      "begin": "Jan 26, 2017 12:00:00 AM",
	      "end": "Jan 27, 2017 12:00:00 AM",
	      "count": 1
	    }
	  ],
	  "mode": "YMD"
	},
	"dataName": "articleDateFiles"
	}	
	}
	 * </pre>
	 * 
	 * 那么通过表达式 {@code data->data->files[0]->begin} 将会返回Jan 26, 2017 12:00:00 AM<br>
	 * 通过表达式{@code data-data->files} 将会返回
	 * 
	 * <pre>
	 [{
	 "begin": "Jan 26, 2017 12:00:00 AM",
	 "end": "Jan 27, 2017 12:00:00 AM",
	 "count": 1
	 }]
	 * </pre>
	 * </p>
	 * 
	 * @author mhlx
	 *
	 */
	public static final class ExpressionExecutor {

		private static final Expression NULL_EXPRESSION = new NullExpression();

		private final JsonElement ele;

		private final Throwable ex;

		private ExpressionExecutor(JsonElement ele) {
			this(ele, null);
		}

		private ExpressionExecutor(JsonElement ele, Throwable ex) {
			this.ele = ele;
			this.ex = ex;
		}

		/**
		 * 执行一个表达式
		 * 
		 * <pre>
		 * data:{
		 * 	data:{
		 * 		success:true
		 * 	}
		 * }
		 * executeForExecutor(data).executeForExecutor(data).executeForExecutor(success).get() ==> 'true'
		 * </pre>
		 * 
		 * @param expression
		 * @return
		 */
		public ExpressionExecutor executeForExecutor(String expression) {
			return new ExpressionExecutor(doExecute(expression));
		}

		/**
		 * 执行一个表达式
		 * 
		 * <pre>
		 * data:{
		 * 	data:{
		 * 		success:true
		 * 	}
		 * }
		 * executeForExecutors(data).getExpressionExecutor(0).executeForExecutor(data).executeForExecutor(success).get() ==> 'true'
		 * 
		 * 
		 * data:{
		 * 	datas:[
		 * 	{
		 * 		success:true
		 * 	},{
		 * 		success:false
		 * 	}	
		 * 	]
		 * }
		 * ExpressionExecutors executors = executeForExecutors(data-&gt;datas);
		 * for(int i=0;i&lt;executors.size();i++){
		 * 	ExpressionExecutor executor = executors.get(i);
		 *  executor.execute(success) ==&gt; true,false
		 * }
		 * </pre>
		 * 
		 * @param expression
		 * @return
		 */
		public ExpressionExecutors executeForExecutors(String expression) {
			JsonElement ele = doExecute(expression);
			return new ExpressionExecutors(toJsonArray(ele));
		}

		/**
		 * 执行表达式，并返回结果
		 * 
		 * <pre>
		 * data:{
		 * 	data:{
		 * 		success:true
		 * 	}
		 * }
		 * execute(data) ==> data:{success:true}
		 * execute(data-&gt;data-&gt;success) ==> 'true'
		 * </pre>
		 * 
		 * @param expression
		 * @return
		 */
		public Optional<String> execute(String expression) {
			if (Validators.isEmptyOrNull(expression, true)) {
				return isNull() ? Optional.empty()
						: Optional.of(ele.isJsonPrimitive() ? ele.getAsString() : ele.toString());
			}
			JsonElement executed = doExecute(expression);
			return executed == JsonNull.INSTANCE ? Optional.empty()
					: executed.isJsonPrimitive() ? Optional.of(executed.getAsString())
							: Optional.of(executed.toString());
		}

		public Optional<String> get() {
			return execute(null);
		}

		private JsonElement doExecute(String expression) {
			if (isNull()) {
				return JsonNull.INSTANCE;
			}
			List<Expression> expressionList = parseExpressions(expression);
			JsonElement executed = null;
			for (Expression exp : expressionList) {
				if (exp == NULL_EXPRESSION) {
					return JsonNull.INSTANCE;
				}
				if (executed == null) {
					executed = exp.get(ele);
				} else {
					executed = exp.get(executed);
				}
			}
			return executed;
		}

		/**
		 * 结果是否为空
		 * 
		 * @return
		 */
		public boolean isNull() {
			return ele == JsonNull.INSTANCE;
		}

		public Throwable getEx() {
			return ex;
		}

		private static List<Expression> parseExpressions(String expression) {
			expression = expression.replaceAll("\\s+", "");
			if (expression.isEmpty()) {
				return List.of(NULL_EXPRESSION);
			}
			if (expression.contains(SPLIT_STR)) {
				// multi expressions
				List<Expression> expressionList = new ArrayList<>();
				for (String _expression : expression.split(SPLIT_STR)) {
					_expression = _expression.replaceAll("\\s+", "");
					if (_expression.isEmpty()) {
						return List.of(NULL_EXPRESSION);
					}
					Expression parsed = parseExpression(_expression);
					if (parsed == NULL_EXPRESSION) {
						return List.of(NULL_EXPRESSION);
					}
					expressionList.add(parsed);
				}
				return expressionList;
			}
			return List.of(parseExpression(expression));
		}

		private static Expression parseExpression(String expression) {
			String indexStr = StringUtils.substringBetween(expression, "[", "]");
			if (indexStr != null) {
				try {
					int index = Integer.parseInt(indexStr);
					String _expression = expression.substring(0, expression.indexOf('[')).trim();
					if (!_expression.isEmpty()) {
						return new ArrayExpression(_expression, index);
					}
				} catch (NumberFormatException e) {
					LOGGER.debug(e.getMessage(), e);
				}
			} else {
				return new Expression(expression);
			}
			return NULL_EXPRESSION;
		}

		private static class Expression {
			protected final String expression;

			public Expression(String expression) {
				super();
				this.expression = expression;
			}

			JsonElement get(JsonElement ele) {
				if (ele.isJsonObject()) {
					JsonObject jo = ele.getAsJsonObject();
					if (jo.has(expression)) {
						return jo.get(expression);
					}
				}
				return JsonNull.INSTANCE;
			}
		}

		private static class NullExpression extends Expression {
			public NullExpression() {
				super("");
			}

			JsonElement get(JsonElement ele) {
				return JsonNull.INSTANCE;
			}
		}

		private static class ArrayExpression extends Expression {
			private final int index;

			public ArrayExpression(String expression, int index) {
				super(expression);
				this.index = index;
			}

			@Override
			JsonElement get(JsonElement ele) {
				if (ele.isJsonObject()) {
					JsonObject jo = ele.getAsJsonObject();
					if (jo.has(expression)) {
						JsonElement expressionEle = jo.get(expression);
						if (expressionEle.isJsonArray()) {
							JsonArray array = expressionEle.getAsJsonArray();
							if (index >= 0 && index <= array.size() - 1) {
								return array.get(index);
							}
						}
					}
				}
				return JsonNull.INSTANCE;
			}
		}

		@Override
		public String toString() {
			return ele == JsonNull.INSTANCE ? null : ele.isJsonPrimitive() ? ele.getAsString() : ele.toString();
		}
	}

	public static final class ExpressionExecutors implements Iterable<ExpressionExecutor> {
		private final JsonArray array;
		private final Throwable ex;

		public ExpressionExecutors(JsonArray array, Throwable ex) {
			super();
			this.array = array;
			this.ex = ex;
		}

		public ExpressionExecutors(JsonArray array) {
			this(array, null);
		}

		public int size() {
			return array.size();
		}

		public ExpressionExecutor getExpressionExecutor(int index) {
			return new ExpressionExecutor(array.get(index));
		}

		public Throwable getEx() {
			return ex;
		}

		@Override
		public Iterator<ExpressionExecutor> iterator() {
			final Iterator<JsonElement> it = array.iterator();
			return new Iterator<Jsons.ExpressionExecutor>() {

				@Override
				public ExpressionExecutor next() {
					return new ExpressionExecutor(it.next());
				}

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}
			};
		}
	}

	private static final class DateSerializer implements JsonSerializer<Date> {

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null) {
				return JsonNull.INSTANCE;
			}
			return new JsonPrimitive(src.getTime());
		}
	}

	private static final class DateDeserializer implements JsonDeserializer<Timestamp> {

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

	private static JsonArray toJsonArray(JsonElement ele) {
		if (ele.isJsonArray()) {
			return ele.getAsJsonArray();
		}
		JsonArray array = new JsonArray(1);
		array.add(ele);
		return array;
	}

}
