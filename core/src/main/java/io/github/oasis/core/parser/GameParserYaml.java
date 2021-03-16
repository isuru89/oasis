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
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Texts;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Parses a game yaml file recursively if specified with references.
 *
 * @author Isuru Weerarathna
 */
public class GameParserYaml implements GameParseSupport {

    private static final String VERSION_KEY = "version";
    private static final String ELEMENTS_KEY = "elements";
    private static final String ELEMENT_PLUGIN_KEY = "type";

    public static GameDef fromFile(File file) throws OasisParseException {
        try (InputStream is = new FileInputStream(file)) {
            var fsContext = new FileParserContext(file.getCanonicalPath());
            return new GameParserYaml().parse(is, fsContext);
        } catch (IOException e) {
            throw new OasisParseException("Unable to parse game definition file in " + file.getAbsolutePath() + "!", e);
        }
    }

    public static GameDef fromClasspath(String clzPathFile, ClassLoader clzLoader) throws OasisParseException {
        var clzPathContext = new ClasspathParserContext(clzPathFile, clzLoader);

        try (InputStream is = clzLoader.getResourceAsStream(clzPathFile)) {
            return new GameParserYaml().parse(is, clzPathContext);
        } catch (IOException e) {
            throw new OasisParseException("Unable to parse game definition file in " + clzPathFile + "!", e);
        }
    }

    public GameDef parse(InputStream is, ParserContext parserContext) throws OasisParseException {
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
                        throw new OasisParseException("Element does not have specified the associated type!");
                    }
                    EngineMessage engineMessage = new EngineMessage();
                    engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
                    engineMessage.setImpl(elementPluginType);
                    engineMessage.setData(elementDef);
                    gameDef.addRuleDefinition(engineMessage);
                } else if (def instanceof String) {
                    String refPath = (String) def;

                    String fullPath = parserContext.manipulateFullPath(refPath);
                    parserContext.pushCurrentLocation(fullPath);
                    InputStream refData = parserContext.loadPath(fullPath);
                    GameDef referencedGameDef = this.parse(refData, parserContext);
                    parserContext.popCurrentLocation();

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
