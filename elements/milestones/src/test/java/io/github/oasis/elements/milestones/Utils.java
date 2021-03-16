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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.external.messages.EngineMessage;
import org.junit.jupiter.api.Assertions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
class Utils {

    private static Map<String, Object> loadGroupFile(String resourcePath) {
        try (InputStream resourceAsStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourcePath)) {
            return new Yaml().load(resourceAsStream);
        } catch (IOException e) {
            Assertions.fail("Cannot load resource " + resourcePath);
            throw new RuntimeException(e);
        }
    }

    private static EngineMessage asPersistedDef(Map<String, Object> data) {
        EngineMessage def = new EngineMessage();
        def.setData(data);
        def.setType(EngineMessage.GAME_RULE_ADDED);
        def.setImpl(MilestoneDef.class.getName());
        return def;
    }

    @SuppressWarnings("unchecked")
    public static List<MilestoneDef> parseAll(String resourcePath, MilestoneParser parser) {
        Map<String, Object> map = loadGroupFile(resourcePath);
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("elements");
        return items.stream().map(Utils::asPersistedDef)
                .map(parser::parse)
                .collect(Collectors.toList());
    }

    public static Optional<MilestoneDef> findByName(List<MilestoneDef> milestoneDefs, String name) {
        return milestoneDefs.stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst();
    }

    public static Optional<MilestoneDef> loadByName(String resourcePath, MilestoneParser parser, String name) {
        List<MilestoneDef> defs = parseAll(resourcePath, parser);
        return findByName(defs, name);
    }

    public static boolean isNumber(Object value) {
        return value instanceof Number;
    }

    public static boolean isNonEmptyString(Object value) {
        return value instanceof String && !((String) value).isBlank();
    }


}
