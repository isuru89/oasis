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
import io.github.oasis.elements.badges.processors.BadgeFirstEvent;
import io.github.oasis.elements.badges.processors.ConditionalBadgeProcessor;
import io.github.oasis.elements.badges.processors.PeriodicBadgeProcessor;
import io.github.oasis.elements.badges.processors.PeriodicStreakNBadge;
import io.github.oasis.elements.badges.processors.StreakNBadgeProcessor;
import io.github.oasis.elements.badges.processors.TimeBoundedStreakNBadge;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesStreakNRule;
import io.github.oasis.elements.badges.rules.PeriodicStreakNRule;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.rules.TimeBoundedStreakNRule;
import io.github.oasis.elements.badges.spec.BadgeSpecification;
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
        var supportedDefinitions = module.getParser().getAcceptingDefinitions().getDefinitions();
        Assertions.assertEquals(1, supportedDefinitions.size());
        Assertions.assertEquals(supportedDefinitions.get(0).getKey(), BadgesModule.ID);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getDefinitionClz(), BadgeDef.class);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getSpecificationClz(), BadgeSpecification.class);

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

        FirstEventBadgeRule rule = Mockito.mock(FirstEventBadgeRule.class);
        Assertions.assertTrue(module.createProcessor(rule, contextSupport) instanceof BadgeFirstEvent);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(ConditionalBadgeRule.class), contextSupport) instanceof ConditionalBadgeProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(StreakNBadgeRule.class), contextSupport) instanceof StreakNBadgeProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(PeriodicBadgeRule.class), contextSupport) instanceof PeriodicBadgeProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(PeriodicOccurrencesRule.class), contextSupport) instanceof PeriodicBadgeProcessor);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(TimeBoundedStreakNRule.class), contextSupport) instanceof TimeBoundedStreakNBadge);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(PeriodicStreakNRule.class), contextSupport) instanceof PeriodicStreakNBadge);
        Assertions.assertTrue(module.createProcessor(Mockito.mock(PeriodicOccurrencesStreakNRule.class), contextSupport) instanceof PeriodicStreakNBadge);
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