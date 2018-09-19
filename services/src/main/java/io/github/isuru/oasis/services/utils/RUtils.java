package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * @author iweerarathna
 */
public class RUtils {

    public static int generateNonce() {
        return new Random(System.currentTimeMillis()).nextInt(999999) + 1;
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> T toObj(String value, Class<T> clz, ObjectMapper mapper) {
        try {
            return mapper.readValue(value, clz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize given db object!", e);
        }
    }

    public static String toStr(Object value, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize given object " + value + "!", e);
        }
    }
}
