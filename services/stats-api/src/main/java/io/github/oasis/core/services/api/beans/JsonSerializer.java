/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.services.SerializationSupport;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Component("json")
public class JsonSerializer implements SerializationSupport {

    private final ObjectMapper mapper;
    private final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
    };

    public JsonSerializer(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    @Override
    public <T, P> T deserializeParameterized(String data, Class<T> mainClass, Class<P> parameterClz) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, mapper.getTypeFactory().constructParametricType(mainClass, parameterClz));
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize given object!", e);
        }
    }

    @Override
    public <T> List<T> deserializeList(String data, Class<T> listType) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, mapper.getTypeFactory().constructCollectionType(ArrayList.class, listType));
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize given object!", e);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> clz) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, clz);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize given object!", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        try {
            return mapper.readValue(new InputStreamReader(new ByteArrayInputStream(data)), clz);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize given object!", e);
        }
    }

    @Override
    public Map<String, Object> deserializeToMap(byte[] data) {
        try {
            return mapper.readValue(new InputStreamReader(new ByteArrayInputStream(data)), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize given object!", e);
        }
    }

    @Override
    public String serialize(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize given object!", e);
        }
    }
}
