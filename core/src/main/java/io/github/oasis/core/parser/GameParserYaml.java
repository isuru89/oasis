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

package io.github.oasis.core.parser;

import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.external.messages.PersistedDef;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Parses a game yaml file.
 *
 * @author Isuru Weerarathna
 */
public class GameParserYaml implements GameParseSupport {

    private static final String VERSION = "version";

    public GameDef parse(Object input) throws OasisParseException {
        if (input instanceof String) {
            String resource = (String) input;
            File file = new File(resource);
            try (InputStream is = new FileInputStream(file)) {
                return parse(is);
            } catch (IOException e) {
                throw new OasisParseException("Unable to parse " + resource + "!", e);
            }
        } else if (input instanceof InputStream) {
            return parse((InputStream) input);
        }
        throw new OasisParseException("Unknown input format provided for parsing!");
    }

    private GameDef parse(InputStream is) {
        Yaml yaml = new Yaml();

        Map<String, Object> dataDef = yaml.load(is);
        GameDef def = new GameDef();
        dataDef.forEach((key, value) -> {
            if (VERSION.equals(key)) {
                def.setVersion((int) value);
            } else {
                parseDefinitions(key, value, def);
            }
        });

        return def;
    }

    @SuppressWarnings("unchecked")
    private void parseDefinitions(String key, Object value, GameDef gameDef) {
        if (value instanceof List) {
            List<Object> defs = (List<Object>) value;
            for (Object def : defs) {
                if (def instanceof Map) {
                    PersistedDef persistedDef = new PersistedDef();
                    persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
                    persistedDef.setImpl(key.toLowerCase());
                    persistedDef.setData((Map<String, Object>) def);
                    gameDef.addRuleDefinition(persistedDef);
                }
            }
        }
    }
}
