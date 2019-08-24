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

package io.github.oasis.model.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisUtils {

    public static String getEnvOr(String key, String defVal) {
        String getenv = System.getenv(key);
        if (getenv != null && !getenv.isEmpty()) {
            return getenv;
        }
        return defVal;
    }

    public static String getEnvOr(String envKey, String jvmPropKey, String defValue) {
        return getEnvOr(envKey, System.getProperty(jvmPropKey, defValue));
    }

    public static Map<String, Object> filterKeys(Properties properties, String keyPfx) {
        Map<String, Object> map = new HashMap<>();
        for (Object keyObj : properties.keySet()) {
            String key = String.valueOf(keyObj);
            if (key.startsWith(keyPfx)) {
                Object val = properties.get(key);
                String tmp = key.substring(keyPfx.length());
                map.put(tmp, val);
            }
        }
        return map;
    }

}
