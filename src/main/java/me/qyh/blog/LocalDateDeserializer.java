package me.qyh.blog;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import me.qyh.blog.utils.StringUtils;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String str = p.getValueAsString();
		if (StringUtils.isNullOrBlank(str)) {
			return null;
		}
		try {
			return LocalDate.parse(str, dtf);
		} catch (DateTimeParseException e) {
			throw new JsonParseException(p, "can not parse value to LocalDate,the value is:" + str);
		}
	}

}
