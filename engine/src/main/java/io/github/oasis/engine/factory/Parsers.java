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

package io.github.oasis.engine.factory;

import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.ElementParser;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.engine.EngineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class Parsers {

    private static final Logger LOG = LoggerFactory.getLogger(Parsers.class);

    private final Map<String, ElementParser> parserCache = new ConcurrentHashMap<>();

    public static Parsers from(EngineContext context) {
        Parsers parsers = new Parsers();
        parsers.init(context);
        return parsers;
    }

    private void init(EngineContext context) {
        context.getModuleList()
                .forEach(mod -> {
                    mod.getSupportedDefinitions().forEach(def -> {
                        ElementParser parser = mod.getParser();
                        parserCache.put(def.getName(), parser);
                        LOG.info("Definition {} will be parsed with {}", def.getName(), parser.getClass().getName());
                    });
                    mod.getSupportedDefinitionKeys().forEach(key -> parserCache.put(key.toLowerCase(), mod.getParser()));
                });
    }

    public AbstractRule parseToRule(PersistedDef dto) {
        String type = dto.getImpl();
        ElementParser elementParser = parserCache.get(type);
        if (elementParser != null) {
            return elementParser.parseToRule(dto);
        }
        throw new IllegalArgumentException("Unknown element type '" + dto.getImpl() + "'!");
    }

}
