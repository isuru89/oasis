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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.elements.challenges.spec.ChallengeFeedData;
import io.github.oasis.elements.challenges.spec.ChallengeSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class ChallengesModuleTest {

    private ChallengesModule module;

    @BeforeEach
    void beforeEach() {
        module = new ChallengesModule();
    }

    @Test
    void shouldHaveCorrectId() {
        Assertions.assertEquals(ChallengesModule.ID, module.getId());
    }

    @Test
    void getSupportedDefinitions() {
        var supportedDefinitions = module.getParser().getAcceptingDefinitions().getDefinitions();
        Assertions.assertEquals(1, supportedDefinitions.size());
        Assertions.assertEquals(supportedDefinitions.get(0).getKey(), ChallengesModule.ID);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getDefinitionClz(), ChallengeDef.class);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getSpecificationClz(), ChallengeSpecification.class);
    }

    @Test
    void shouldDeclareCorrectFeedDefinitions() {
        var def = module.getFeedDefinitions();
        Assertions.assertEquals(1, def.size());
        Assertions.assertTrue(def.containsKey(ChallengeIDs.FEED_TYPE_CHALLENGE_WON));
        Assertions.assertEquals(ChallengeFeedData.class, def.get(ChallengeIDs.FEED_TYPE_CHALLENGE_WON));
    }

    @Test
    void getParser() {
        Assertions.assertTrue(module.getParser() instanceof ChallengeParser);
    }

    @Test
    void getSupportedSinks() {
        List<Class<? extends AbstractSink>> supportedSinks = module.getSupportedSinks();
        Assertions.assertEquals(1, supportedSinks.size());
        Assertions.assertTrue(supportedSinks.contains(ChallengesSink.class));
    }

    @Test
    void createSink() {
        RuntimeContextSupport contextSupport = Mockito.mock(RuntimeContextSupport.class);
        Db db = Mockito.mock(Db.class);

        Mockito.when(contextSupport.getDb()).thenReturn(db);

        AbstractSink firstSink = module.createSink(ChallengesSink.class, contextSupport);
        AbstractSink secondSink = module.createSink(ChallengesSink.class, contextSupport);
        Assertions.assertNotEquals(firstSink, secondSink);
    }

    @Test
    void createProcessor() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);
        SignalCollector signalCollector = Mockito.mock(SignalCollector.class);

        Mockito.when(contextSupport.getSignalCollector()).thenReturn(signalCollector);

        ChallengeRule rule = Mockito.mock(ChallengeRule.class);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> firstProcessor = module.createProcessor(rule, contextSupport);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> secondProcessor = module.createProcessor(rule, contextSupport);

        Assertions.assertTrue(firstProcessor instanceof ChallengeProcessor);
        Assertions.assertTrue(secondProcessor instanceof ChallengeProcessor);
        Assertions.assertNotEquals(firstProcessor, secondProcessor);
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