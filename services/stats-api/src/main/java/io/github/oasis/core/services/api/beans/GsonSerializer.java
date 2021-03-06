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

import com.google.gson.Gson;
import io.github.oasis.core.services.SerializationSupport;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * @author Isuru Weerarathna
 */
@Component("gson")
public class GsonSerializer implements SerializationSupport {

    private final Gson gson;

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(String data, Class<T> clz) {
        return gson.fromJson(data, clz);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), clz);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), type);
    }

    @Override
    public String serialize(Object data) {
        return gson.toJson(data);
    }
}
