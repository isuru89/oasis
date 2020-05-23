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

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class MilestonesModuleTest {

    private MilestonesModule module;

    @BeforeEach
    void beforeEach() {
        module = new MilestonesModule();
    }

    @Test
    void getSupportedDefinitions() {
        List<Class<? extends AbstractDef>> supportedDefinitions = module.getSupportedDefinitions();
        Assertions.assertEquals(1, supportedDefinitions.size());
        Assertions.assertTrue(supportedDefinitions.contains(MilestoneDef.class));
    }

    @Test
    void getParser() {
        Assertions.assertTrue(module.getParser() instanceof MilestoneParser);
    }

    @Test
    void getSupportedSinks() {
        List<Class<? extends AbstractSink>> supportedSinks = module.getSupportedSinks();
        Assertions.assertEquals(1, supportedSinks.size());
        Assertions.assertTrue(supportedSinks.contains(MilestonesSink.class));
    }

    @Test
    void createSink() {
        RuntimeContextSupport contextSupport = Mockito.mock(RuntimeContextSupport.class);
        Db db = Mockito.mock(Db.class);

        Mockito.when(contextSupport.getDb()).thenReturn(db);

        AbstractSink firstSink = module.createSink(MilestonesSink.class, contextSupport);
        AbstractSink secondSink = module.createSink(MilestonesSink.class, contextSupport);
        Assertions.assertNotEquals(firstSink, secondSink);
    }

    @Test
    void createProcessor() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);
        SignalCollector signalCollector = Mockito.mock(SignalCollector.class);

        Mockito.when(contextSupport.getSignalCollector()).thenReturn(signalCollector);

        MilestoneRule rule = Mockito.mock(MilestoneRule.class);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> firstProcessor = module.createProcessor(rule, contextSupport);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> secondProcessor = module.createProcessor(rule, contextSupport);

        Assertions.assertTrue(firstProcessor instanceof MilestoneProcessor);
        Assertions.assertTrue(secondProcessor instanceof MilestoneProcessor);
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