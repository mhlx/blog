package me.qyh.blog;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import me.qyh.blog.utils.StringUtils;
import me.qyh.blog.utils.TimeUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final Map<Integer, String> PATTERNS = Map.of(19, "yyyy-MM-dd HH:mm:ss", 16, "yyyy-MM-dd HH:mm", 10,
            "yyyy-MM-dd");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String str = p.getValueAsString();
        if (StringUtils.isNullOrBlank(str)) {
            return null;
        }
        String pattern = PATTERNS.get(str.length());
        if (pattern == null) {
            throw new JsonParseException(p, "invalid LocalDateTime, the available patterns are " + PATTERNS.values());
        }
        try {
            return TimeUtils.parse(str, pattern);
        } catch (DateTimeParseException e) {
            throw new JsonParseException(p, "can not parse value to LocalDateTime,the value is:" + str);
        }
    }

}
