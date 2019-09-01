/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.utils;

import io.github.oasis.services.exception.InputValidationException;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ValueMap {

    private final Map<String, Object> data;

    public ValueMap(Map<String, Object> data) {
        this.data = data;
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public String getStrReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return (String) data.get(key);
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public String getStr(String key, String def) throws InputValidationException {
        if (data.containsKey(key)) {
            return (String) data.get(key);
        } else {
            return def;
        }
    }

    public long getLongReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Long.parseLong(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public float getFloatReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Float.parseFloat(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public int getIntReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Integer.parseInt(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public long getLong(String key, long defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Long.parseLong(val.toString());
        } else {
            return defVal;
        }
    }

    public float getFloat(String key, float defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Float.parseFloat(val.toString());
        } else {
            return defVal;
        }
    }

    public int getInt(String key, int defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Integer.parseInt(val.toString());
        } else {
            return defVal;
        }
    }
}
