/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.ext.kafkastream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
public class MessageSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String serialize(Object object) throws IOException {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IOException("Provided object cannot be serialized!", e);
        }
    }

    public static <T> T deserialize(String message, Class<T> clz) throws IOException {
        try {
            return MAPPER.readValue(message, clz);
        } catch (JsonProcessingException e) {
            throw new IOException("Provided kafka message cannot be read!", e);
        }
    }

    static <T> T deserialize(String message, TypeReference<T> typeReference) throws IOException {
        try {
            return MAPPER.readValue(message, typeReference);
        } catch (JsonProcessingException e) {
            throw new IOException("Provided kafka message cannot be read!", e);
        }
    }
}
