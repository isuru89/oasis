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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class OasisConfigs implements Serializable {

    public static final String GAME_SUPERVISOR_COUNT = "oasis.supervisors.game";
    public static final String RULE_SUPERVISOR_COUNT = "oasis.supervisors.rule";
    public static final String SIGNAL_SUPERVISOR_COUNT = "oasis.supervisors.signal";
    public static final String RULE_EXECUTOR_COUNT = "oasis.executors.rule";
    public static final String SIGNAL_EXECUTOR_COUNT = "oasis.executors.signal";
    public static final String EVENT_STREAM_IMPL = "oasis.eventstream.impl";
    public static final String OASIS_ENGINE_NAME = "oasis.name";
    public static final String OASIS_ENGINE_TIMEZONE = "oasis.timeZone";

    private static final String DEFAULT_ENGINE_NAME = "oasis-engine";

    private final Config props;

    private final ZoneId engineZoneId = ZoneId.systemDefault();
    private int tzOffset = Integer.MAX_VALUE;

    private OasisConfigs(Config configs) {
        props = configs;
    }

    public static OasisConfigs create(Map<String, Object> configMap) {
        Config config = ConfigFactory.parseMap(configMap);
        return new OasisConfigs(config);
    }

    public static OasisConfigs create(Config config) {
        return new OasisConfigs(config);
    }

    public static OasisConfigs create(String filePath) {
        Config config = ConfigFactory.parseFile(new File(filePath));
        return new OasisConfigs(config);
    }

    public static OasisConfigs defaultConfigs() {
        Config config = ConfigFactory.load();
        return new OasisConfigs(config);
    }

    public String getEngineTimezone() {
        String zoneId = get(OASIS_ENGINE_TIMEZONE, engineZoneId.getId());
        if (tzOffset == Integer.MAX_VALUE) {
            tzOffset = ZoneId.of(zoneId).getRules().getOffset(Instant.now()).getTotalSeconds();
        }
        return zoneId;
    }

    public int getEngineTimeOffset() {
        if (tzOffset == Integer.MAX_VALUE) {
            getEngineTimezone();
        }
        return tzOffset;
    }

    public String getEngineName() {
        try {
            return props.getString(OASIS_ENGINE_NAME);
        } catch (ConfigException.Missing missing) {
            return DEFAULT_ENGINE_NAME;
        }

    }

    public int getInt(String property, int defaultValue) {
        try {
            return props.getInt(property);
        } catch (ConfigException.Missing missing) {
            return defaultValue;
        }
    }

    public String get(String property, String defaultVal) {
        try {
            return props.getString(property);
        } catch (ConfigException.Missing missing) {
            return defaultVal;
        }
    }

    public Config getConfigRef() {
        return props;
    }

    public static class Builder {
        private final Map<String, Object> map = new HashMap<>();

        public OasisConfigs build() {
            return OasisConfigs.create(map);
        }

        public OasisConfigs minimum() {
            return withSupervisors(1, 1, 1)
                    .withExecutors(1, 1)
                    .build();
        }

        public Builder withSupervisors(int gameSupervisors, int ruleSupervisors, int signalSupervisors) {
            map.put(GAME_SUPERVISOR_COUNT, gameSupervisors);
            map.put(RULE_SUPERVISOR_COUNT, ruleSupervisors);
            map.put(SIGNAL_SUPERVISOR_COUNT, signalSupervisors);
            return this;
        }

        public Builder withExecutors(int ruleExecutors, int signalExecutors) {
            map.put(RULE_EXECUTOR_COUNT, ruleExecutors);
            map.put(SIGNAL_EXECUTOR_COUNT, signalExecutors);
            return this;
        }
    }
}
