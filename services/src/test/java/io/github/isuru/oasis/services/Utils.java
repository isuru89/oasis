package io.github.isuru.oasis.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object val) throws JsonProcessingException {
        return mapper.writeValueAsString(val);
    }

    public static <T> T fromJson(String text, Class<T> clz) throws IOException {
        return mapper.readValue(text, clz);
    }

    public static String toBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

}
