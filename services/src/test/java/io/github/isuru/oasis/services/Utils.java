package io.github.isuru.oasis.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.stream.Collectors;

public class Utils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String text = Files.readAllLines(new File("./scripts/schema.sqlite.sql").toPath())
                .stream().collect(Collectors.joining(","));
    }

    public static String toJson(Object val) throws JsonProcessingException {
        return mapper.writeValueAsString(val);
    }

    public static <T> T fromJson(String text, Class<T> clz) throws IOException {
        return mapper.readValue(text, clz);
    }

    public static <T> T fromJson(byte[] text, Class<T> clz) throws IOException {
        return mapper.readValue(text, clz);
    }

    public static String toBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

}
