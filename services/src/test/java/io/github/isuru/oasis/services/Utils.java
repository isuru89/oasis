package io.github.isuru.oasis.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object val) throws JsonProcessingException {
        return mapper.writeValueAsString(val);
    }

}
