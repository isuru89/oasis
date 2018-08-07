package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class JsonTransformer implements ResponseTransformer {

    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {};

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String render(Object model) throws Exception {
        return mapper.writeValueAsString(model);
    }

    public <T> T parse(String body, Class<T> clz) throws Exception {
        return mapper.readValue(body, clz);
    }

    public Map<String, Object> parseAsMap(String body) throws Exception {
        return mapper.readValue(body, TYPE_REFERENCE);
    }

    public String toStr(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
