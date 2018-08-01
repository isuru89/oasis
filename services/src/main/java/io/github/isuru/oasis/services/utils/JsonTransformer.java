package io.github.isuru.oasis.services.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;

/**
 * @author iweerarathna
 */
public class JsonTransformer implements ResponseTransformer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String render(Object model) throws Exception {
        return mapper.writeValueAsString(model);
    }

    public <T> T parse(String body, Class<T> clz) throws Exception {
        return mapper.readValue(body, clz);
    }
}
