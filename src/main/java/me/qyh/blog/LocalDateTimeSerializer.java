package me.qyh.blog;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider serializers) throws IOException {
		g.writeStartArray();
		g.writeNumber(value.getYear());
		g.writeNumber(value.getMonthValue());
		g.writeNumber(value.getDayOfMonth());
		g.writeNumber(value.getHour());
		g.writeNumber(value.getMinute());
		g.writeNumber(value.getSecond());
		g.writeEndArray();
	}

}
