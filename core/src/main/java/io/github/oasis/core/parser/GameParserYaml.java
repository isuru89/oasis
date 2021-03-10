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
import io.github.oasis.core.utils.Texts;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parses a game yaml file.
 *
 * @author Isuru Weerarathna
 */
public class GameParserYaml implements GameParseSupport {

    private static final String VERSION_KEY = "version";
    private static final String ELEMENTS_KEY = "elements";
    private static final String ELEMENT_PLUGIN_KEY = "plugin";

    public static GameDef fromFile(File file) throws OasisParseException {
        var fsContext = new ParserContext() {
            @Override
            public Object loadSiblingPath(String path) throws OasisParseException {
                try {
                    return new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    throw new OasisParseException("File not found in given path '" + path + "'!");
                }


            }
        };

        try (InputStream is = new FileInputStream(file)) {
            return new GameParserYaml().parse(is, fsContext);
        } catch (IOException e) {
            throw new OasisParseException("Unable to parse game definition file in " + file.getAbsolutePath() + "!", e);
        }
    }

    public static GameDef fromClasspath(String clzPathFile, ClassLoader clzLoader) throws OasisParseException {
        var clzPathContext = new ParserContext() {
            @Override
            public Object loadSiblingPath(String path) throws OasisParseException {
                InputStream is = clzLoader.getResourceAsStream(path);
                if (Objects.isNull(is)) {
                    throw new OasisParseException("Cannot find referenced classpath resource in " + path + "!");
                }
                return is;
            }
        };

        try (InputStream is = clzLoader.getResourceAsStream(clzPathFile)) {
            return new GameParserYaml().parse(is, clzPathContext);
        } catch (IOException e) {
            throw new OasisParseException("Unable to parse game definition file in " + clzPathFile + "!", e);
        }
    }

    public GameDef parse(Object input, ParserContext parserContext) throws OasisParseException {
        if (input instanceof String) {
            String resource = (String) input;
            File file = new File(resource);
            try (InputStream is = new FileInputStream(file)) {
                return parse(is, parserContext);
            } catch (IOException e) {
                throw new OasisParseException("Unable to parse " + resource + "!", e);
            }
        } else if (input instanceof InputStream) {
            return parse((InputStream) input, parserContext);
        }
        throw new OasisParseException("Unknown input format provided for parsing!");
    }

    private GameDef parse(InputStream is, ParserContext parserContext) throws OasisParseException {
        Yaml yaml = new Yaml();

        Map<String, Object> dataDef = yaml.load(is);
        GameDef def = new GameDef();
        def.setVersion((int) dataDef.get(VERSION_KEY));
        Object elementsVal = dataDef.get(ELEMENTS_KEY);
        parseDefinitions(elementsVal, def, parserContext);

        return def;
    }

    @SuppressWarnings("unchecked")
    private void parseDefinitions(Object value, GameDef gameDef, ParserContext parserContext) throws OasisParseException {
        if (value instanceof List) {
            List<Object> definitions = (List<Object>) value;
            for (Object def : definitions) {
                if (def instanceof Map) {
                    var elementDef = (Map<String, Object>) def;
                    String elementPluginType = (String) elementDef.get(ELEMENT_PLUGIN_KEY);
                    if (Texts.isEmpty(elementPluginType)) {
                        throw new OasisParseException("Element does not have specified the associated plugin!");
                    }
                    PersistedDef persistedDef = new PersistedDef();
                    persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
                    persistedDef.setImpl(elementPluginType);
                    persistedDef.setData(elementDef);
                    gameDef.addRuleDefinition(persistedDef);
                } else if (def instanceof String) {
                    String refPath = (String) def;
                    Object refData = parserContext.loadSiblingPath(refPath);
                    GameDef referencedGameDef = this.parse(refData, parserContext);
                    this.mergeGameDefinitions(gameDef, referencedGameDef);
                }
            }
        } else {
            throw new OasisParseException("No elements are defined in the provided source!");
        }
    }

    private void mergeGameDefinitions(GameDef source, GameDef referenced) {
        source.getRuleDefinitions().addAll(referenced.getRuleDefinitions());
    }
}
