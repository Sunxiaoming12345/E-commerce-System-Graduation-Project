package com.example.orderservice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 序列化/反序列化：yyyy-MM-dd HH:mm:ss 北京时间
 */
@JsonComponent
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            gen.writeString(value.format(FORMATTER));
        }
    }

    @JsonComponent
    public static class Deserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            // 纯空格格式（北京时间），直接解析
            if (!text.contains("T") && !text.contains("Z") && !text.contains("+")) {
                return LocalDateTime.parse(text, FORMATTER);
            }
            // 标准 ISO/UTC 格式，带时区转换
            ZonedDateTime zdt;
            if (text.contains("T")) {
                if (text.endsWith("Z")) {
                    zdt = ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")));
                } else {
                    zdt = ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
                }
            } else if (text.endsWith("Z")) {
                text = text.replace(" ", "T");
                zdt = ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")));
            } else {
                text = text.replace(" ", "T");
                zdt = ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            }
            return zdt.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        }
    }
}
