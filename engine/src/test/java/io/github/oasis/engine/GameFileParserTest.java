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

package io.github.oasis.engine;

import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.parser.GameParserYaml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class GameFileParserTest {

    @Test
    public void testParseReferencedFiles() throws OasisParseException {
        GameDef gameDef = GameParserYaml.fromClasspath("rules/root-game-def.yml", Thread.currentThread().getContextClassLoader());
        List<PersistedDef> ruleDefinitions = gameDef.getRuleDefinitions();
        Assertions.assertEquals(5, ruleDefinitions.size());

        Set<String> ids = ruleDefinitions.stream().map(r -> (String) r.getData().get("id")).collect(Collectors.toSet());
        Assertions.assertTrue(ids.contains("BDG00001"));
        Assertions.assertTrue(ids.contains("BDG00002"));
        Assertions.assertTrue(ids.contains("BDG00003"));
        Assertions.assertTrue(ids.contains("MILE000001"));
        Assertions.assertTrue(ids.contains("CHG000001"));
    }

}
