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

package io.github.oasis.elements.badges;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.elements.badges.rules.BadgeConditionalRule;
import io.github.oasis.elements.badges.rules.BadgeFirstEventRule;
import io.github.oasis.elements.badges.rules.BadgeHistogramCountStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeHistogramStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalCountRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalStreakNRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class BadgesModuleTest {

    private BadgesModule module;

    @BeforeEach
    void beforeEach() {
        module = new BadgesModule();
    }

    @Test
    void getSupportedDefinitions() {
        List<Class<? extends AbstractDef>> supportedDefinitions = module.getSupportedDefinitions();
        Assertions.assertEquals(1, supportedDefinitions.size());
        Assertions.assertTrue(supportedDefinitions.contains(BadgeDef.class));
    }

    @Test
    void getParser() {
        Assertions.assertTrue(module.getParser() instanceof BadgeParser);
    }

    @Test
    void getSupportedSinks() {
        List<Class<? extends AbstractSink>> supportedSinks = module.getSupportedSinks();
        Assertions.assertEquals(1, supportedSinks.size());
        Assertions.assertTrue(supportedSinks.contains(BadgeSink.class));
    }

    @Test
    void createSink() {
        RuntimeContextSupport contextSupport = Mockito.mock(RuntimeContextSupport.class);
        Db db = Mockito.mock(Db.class);

        Mockito.when(contextSupport.getDb()).thenReturn(db);

        AbstractSink firstSink = module.createSink(BadgeSink.class, contextSupport);
        AbstractSink secondSink = module.createSink(BadgeSink.class, contextSupport);
        Assertions.assertNotEquals(firstSink, secondSink);
    }

    @Test
    void createProcessor() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);
        SignalCollector signalCollector = Mockito.mock(SignalCollector.class);

        Mockito.when(contextSupport.getSignalCollector()).thenReturn(signalCollector);

        BadgeFirstEventRule rule = Mockito.mock(BadgeFirstEventRule.class);
        Assertions.assertTrue(module.createProcessor(rule, contextSupport) instanceof BadgeFirstEvent);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeConditionalRule.class), contextSupport) instanceof BadgeConditionalProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeStreakNRule.class), contextSupport) instanceof BadgeStreakN);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeTemporalRule.class), contextSupport) instanceof BadgeTemporalProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeTemporalCountRule.class), contextSupport) instanceof BadgeTemporalProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeTemporalStreakNRule.class), contextSupport) instanceof BadgeTemporalStreakN);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeHistogramStreakNRule.class), contextSupport) instanceof BadgeHistogramStreakN);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(BadgeHistogramCountStreakNRule.class), contextSupport) instanceof BadgeHistogramStreakN);
    }

    @Test
    void createProcessorForUnknownRule() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);

        Assertions.assertNull(module.createProcessor(new UnknownRule("abc"), contextSupport));
    }

    private static class UnknownRule extends AbstractRule {
        public UnknownRule(String id) {
            super(id);
        }
    }
}