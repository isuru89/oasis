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

package io.github.oasis.core.configs;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Isuru Weerarathna
 */
public class OasisConfigs implements Serializable {

    private static final String DEFAULT_ENV_PREFIX = "OASIS_";

    public static final String GAME_SUPERVISOR_COUNT = "oasis.supervisors.game";
    public static final String RULE_SUPERVISOR_COUNT = "oasis.supervisors.rule";
    public static final String RULE_EXECUTOR_COUNT = "oasis.executors.rule";
    public static final String SIGNAL_EXECUTOR_COUNT = "oasis.executors.signal";
    public static final String EVENT_STREAM_IMPL = "oasis.eventstream.impl";

    private static final Supplier<Map<String, String>> defaultEnvVarSupplier = System::getenv;

    private final String envVarPrefix;
    private final Map<String, Object> configValues;
    private Map<String, String> envVars;

    private final Configuration jsonPathConf = Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();

    private DocumentContext ctx;

    private OasisConfigs(Map<String, Object> configValues, String envVarPrefix, Supplier<Map<String, String>> envVarSupplier) {
        this.envVarPrefix = Objects.toString(envVarPrefix, DEFAULT_ENV_PREFIX);
        this.configValues = configValues;

        this.loadEnvVariables(Objects.requireNonNullElse(envVarSupplier, defaultEnvVarSupplier));
        this.createConfigContext(configValues);
    }

    private void createConfigContext(Map<String, Object> configValues) {
        var jsonObject = new JsonObject(configValues);
        ctx = JsonPath.parse(jsonObject.toJson(), jsonPathConf);
    }

    private void loadEnvVariables(Supplier<Map<String, String>> envVarSupplier) {
        var envVarsTmp = envVarSupplier.get();
        this.envVars = new HashMap<>();

        envVarsTmp.forEach((k, v) -> {
            if (k.startsWith(this.envVarPrefix)) {
                envVars.put(k.substring(this.envVarPrefix.length()), v);
            }
        });
    }

    private Optional<String> readFromEnvVar(String prop) {
        String envProp = prop.replaceAll("\\.", "_").toUpperCase();
        return Optional.ofNullable(this.envVars.get(envProp));
    }

    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(configValues);
    }

    private String getValue(String property, String defaultValue) {
        return readFromEnvVar(property).orElseGet(() -> {
            String value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public int getInt(String property, int defaultValue) {
        return readFromEnvVar(property).map(Integer::parseInt).orElseGet(() -> {
            Integer value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public long getLong(String property, long defaultValue) {
        return readFromEnvVar(property).map(Long::parseLong).orElseGet(() -> {
            Long value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public float getFloat(String property, float defaultValue) {
        return readFromEnvVar(property).map(Float::parseFloat).orElseGet(() -> {
            Float value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public double getDouble(String property, double defaultValue) {
        return readFromEnvVar(property).map(Double::parseDouble).orElseGet(() -> {
            Double value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public boolean getBoolean(String property, boolean defaultValue) {
        return readFromEnvVar(property).map(Boolean::parseBoolean).orElseGet(() -> {
            Boolean value = ctx.read(toQuery(property));
            return value != null ? value : defaultValue;
        });
    }

    public String get(String property, String defaultVal) {
        return getValue(property, defaultVal);
    }

    public Map<String, Object> getObject(String property) {
        Map<String, Object> value = ctx.read(toQuery(property));
        return resolveWithEnvVars(value, property);
    }

    private Map<String, Object> resolveWithEnvVars(Map<String, Object> configs, String baseProperty) {
        var result = new HashMap<String, Object>();

        configs.forEach((k, v) -> {
            if (v instanceof Boolean) {
                result.put(k, getBoolean(concat(baseProperty, k), (Boolean) v));
            } else if (v instanceof String) {
                result.put(k, getValue(concat(baseProperty, k), (String) v));
            } else if (v instanceof Integer) {
                result.put(k, getInt(concat(baseProperty, k), (Integer) v));
            } else if (v instanceof Long) {
                result.put(k, getLong(concat(baseProperty, k), (Long) v));
            } else if (v instanceof Float) {
                result.put(k, getFloat(concat(baseProperty, k), (Float) v));
            } else if (v instanceof Double) {
                result.put(k, getDouble(concat(baseProperty, k), (Double) v));
            } else {
                result.put(k, v);
            }
        });

        return result;
    }

    private String toQuery(String property) {
        return concat(null, property);
    }

    private String concat(String base, String property) {
        if (base == null) {
            return "$." + property;
        } else {
            return base + "." + property;
        }
    }

    public static class Builder {
        private final Map<String, Object> map = new HashMap<>();
        private String envVariablePrefix;
        private Supplier<Map<String, String>> envSupplier = null;

        public OasisConfigs build() {
            return new OasisConfigs(map, envVariablePrefix, envSupplier);
        }

        public OasisConfigs buildWithConfigs(Map<String, Object> configs) {
            return new OasisConfigs(configs, envVariablePrefix, envSupplier);
        }

        public OasisConfigs buildFromYamlResource(String resourcePath) {
            return buildFromYamlResource(resourcePath, Thread.currentThread().getContextClassLoader());
        }

        public OasisConfigs buildFromYamlResource(String resourcePath, ClassLoader clzLoader) {
            Yaml yaml = new Yaml();
            try (var inputStream = clzLoader.getResourceAsStream(resourcePath)) {
                Map<String, Object> map = yaml.load(inputStream);
                return buildWithConfigs(map);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error parsing config file!", e);
            }
        }

        public OasisConfigs buildFromYamlFile(String filePath) {
            Yaml yaml = new Yaml();
            try (var inputStream = new FileInputStream(filePath)) {
                Map<String, Object> map = yaml.load(inputStream);
                return buildWithConfigs(map);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error parsing config file!", e);
            }
        }

        public Builder withCustomEnvOverrides(Map<String, String> map) {
            envSupplier = () -> map;
            return this;
        }

        public Builder withEnvVariablePrefix(String prefix) {
            this.envVariablePrefix = prefix;
            return this;
        }

    }
}
