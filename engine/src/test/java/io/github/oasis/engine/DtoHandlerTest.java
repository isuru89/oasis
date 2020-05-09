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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.actors.cmds.RuleRemovedMessage;
import io.github.oasis.engine.actors.cmds.RuleUpdatedMessage;
import io.github.oasis.engine.factory.Parsers;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Message Transformation")
public class DtoHandlerTest {

    private static final int GAME_ID = 1;
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<Map<String, Object>>() {}.getType();

    @Test
    @DisplayName("Parsing Event Messages")
    void testEventParsing() {
        PersistedDef eventDef = createDef(PersistedDef.GAME_EVENT, null, GAME_ID);
        eventDef.setData(objToMap(TEvent.createKeyValue(System.currentTimeMillis(), "event.a", 100)));
        Object derived = DtoHandler.derive(eventDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(EventJson.class.getName(), derived.getClass().getName());
    }

    @Test
    @DisplayName("Parsing Game Add Messages")
    void testGameAddParsing() {
        PersistedDef gameDef = createDef(PersistedDef.GAME_ADDED, null, GAME_ID);
        GameCommand derived = (GameCommand) DtoHandler.derive(gameDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(GameCommand.GameLifecycle.CREATE, derived.getStatus());
        Assertions.assertEquals(GAME_ID, derived.getGameId());
    }

    @Test
    @DisplayName("Parsing Game Remove Messages")
    void testGameRemoveParsing() {
        PersistedDef gameDef = createDef(PersistedDef.GAME_REMOVED, null, GAME_ID);
        GameCommand derived = (GameCommand) DtoHandler.derive(gameDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(GameCommand.GameLifecycle.REMOVE, derived.getStatus());
        Assertions.assertEquals(GAME_ID, derived.getGameId());
    }

    @Test
    @DisplayName("Parsing Game Update Messages")
    void testGameUpdateParsing() {
        PersistedDef gameDef = createDef(PersistedDef.GAME_UPDATED, null, GAME_ID);
        GameCommand derived = (GameCommand) DtoHandler.derive(gameDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(GameCommand.GameLifecycle.UPDATE, derived.getStatus());
        Assertions.assertEquals(GAME_ID, derived.getGameId());
    }

    @Test
    @DisplayName("Parsing Game Start Messages")
    void testGameStartParsing() {
        PersistedDef gameDef = createDef(PersistedDef.GAME_STARTED, null, GAME_ID);
        GameCommand derived = (GameCommand) DtoHandler.derive(gameDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(GameCommand.GameLifecycle.START, derived.getStatus());
        Assertions.assertEquals(GAME_ID, derived.getGameId());
    }

    @Test
    @DisplayName("Parsing Game Pause Messages")
    void testGamePauseParsing() {
        PersistedDef gameDef = createDef(PersistedDef.GAME_PAUSED, null, GAME_ID);
        GameCommand derived = (GameCommand) DtoHandler.derive(gameDef, null);
        Assertions.assertNotNull(derived);
        Assertions.assertEquals(GameCommand.GameLifecycle.PAUSE, derived.getStatus());
        Assertions.assertEquals(GAME_ID, derived.getGameId());
    }

    @Test
    @DisplayName("Parsing Rule Add Messages")
    void testRuleAddParsing() {
        TestRule testRule = new TestRule(UUID.randomUUID().toString());
        Parsers parsers = Mockito.mock(Parsers.class);
        EngineContext context = Mockito.mock(EngineContext.class);
        Mockito.when(parsers.parseToRule(Mockito.any())).thenReturn(testRule);
        Mockito.when(context.getParsers()).thenReturn(parsers);

        PersistedDef ruleDef = createDef(PersistedDef.GAME_RULE_ADDED, "any", GAME_ID);
        OasisRuleMessage derived = (OasisRuleMessage) DtoHandler.derive(ruleDef, context);
        Assertions.assertNotNull(derived);
        Assertions.assertTrue(derived instanceof RuleAddedMessage, "should be rule added message!");
        Assertions.assertEquals(GAME_ID, derived.getGameId());
        Assertions.assertSame(testRule, ((RuleAddedMessage) derived).getRule());
    }

    @Test
    @DisplayName("Parsing Rule Update Messages")
    void testRuleUpdateParsing() {
        TestRule testRule = new TestRule(UUID.randomUUID().toString());
        Parsers parsers = Mockito.mock(Parsers.class);
        EngineContext context = Mockito.mock(EngineContext.class);
        Mockito.when(parsers.parseToRule(Mockito.any())).thenReturn(testRule);
        Mockito.when(context.getParsers()).thenReturn(parsers);

        PersistedDef ruleDef = createDef(PersistedDef.GAME_RULE_UPDATED, "any", GAME_ID);
        OasisRuleMessage derived = (OasisRuleMessage) DtoHandler.derive(ruleDef, context);
        Assertions.assertNotNull(derived);
        Assertions.assertTrue(derived instanceof RuleUpdatedMessage, "should be rule updated message!");
        Assertions.assertEquals(GAME_ID, derived.getGameId());
        Assertions.assertSame(testRule, ((RuleUpdatedMessage) derived).getRule());
    }

    @Test
    @DisplayName("Parsing Rule Removed Messages")
    void testRuleRemovedParsing() {
        TestRule testRule = new TestRule(UUID.randomUUID().toString());
        Parsers parsers = Mockito.mock(Parsers.class);
        EngineContext context = Mockito.mock(EngineContext.class);
        Mockito.when(parsers.parseToRule(Mockito.any())).thenReturn(testRule);
        Mockito.when(context.getParsers()).thenReturn(parsers);

        PersistedDef ruleDef = createDef(PersistedDef.GAME_RULE_REMOVED, "any", GAME_ID);
        OasisRuleMessage derived = (OasisRuleMessage) DtoHandler.derive(ruleDef, context);
        Assertions.assertNotNull(derived);
        Assertions.assertTrue(derived instanceof RuleRemovedMessage, "should be rule removed message!");
        Assertions.assertEquals(GAME_ID, derived.getGameId());
        Assertions.assertEquals(testRule.getId(), ((RuleRemovedMessage) derived).getRuleId());
    }

    @Test
    @DisplayName("Parsing unknown message")
    void testUnknownMessage() {
        PersistedDef def = createDef("unknown", "any", 2);
        Object derive = DtoHandler.derive(def, null);
        Assertions.assertNull(derive);
    }

    private Map<String, Object> objToMap(Object object) {
        String jsonStr = gson.toJson(object);
        return gson.fromJson(jsonStr, type);
    }

    private PersistedDef createDef(String type, String impl, int gameId) {
        PersistedDef def = new PersistedDef();
        def.setScope(new PersistedDef.Scope(gameId));
        def.setImpl(impl);
        def.setType(type);
        return def;
    }

    private static class TestRule extends AbstractRule {

        public TestRule(String id) {
            super(id);
        }
    }
}
