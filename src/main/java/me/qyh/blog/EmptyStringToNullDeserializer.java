package me.qyh.blog;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

public class EmptyStringToNullDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		String value = StringDeserializer.instance.deserialize(jsonParser, context);
		if (value != null && value.isEmpty()) {
			return null;
		}
		return value;
	}

}