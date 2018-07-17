package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class RUtils {

    @SuppressWarnings("unchecked")
    public static Class<?> loadClz(String clzName) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(clzName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static <T> T toObj(String value, Class<T> clz, ObjectMapper mapper) {
        try {
            return mapper.readValue(value, clz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize given db object!");
        }
    }

    public static String toStr(Object value, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize given object " + value + "!");
        }
    }
}
