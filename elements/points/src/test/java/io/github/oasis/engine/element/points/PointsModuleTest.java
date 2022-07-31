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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.engine.element.points.spec.PointSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class PointsModuleTest {

    private PointsModule pointsModule;

    @BeforeEach
    void beforeEach() {
        pointsModule = new PointsModule();
    }

    @Test
    void getSupportedDefinitions() {
        var supportedDefinitions = pointsModule.getParser().getAcceptingDefinitions().getDefinitions();
        Assertions.assertEquals(1, supportedDefinitions.size());
        Assertions.assertEquals(supportedDefinitions.get(0).getKey(), PointsModule.ID);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getDefinitionClz(), PointDef.class);
        Assertions.assertEquals(supportedDefinitions.get(0).getAcceptedDefinitions().getSpecificationClz(), PointSpecification.class);
    }

    @Test
    void getParser() {
        Assertions.assertTrue(pointsModule.getParser() instanceof PointParser);
    }

    @Test
    void getSupportedSinks() {
        List<Class<? extends AbstractSink>> supportedSinks = pointsModule.getSupportedSinks();
        Assertions.assertEquals(1, supportedSinks.size());
        Assertions.assertTrue(supportedSinks.contains(PointsSink.class));
    }

    @Test
    void createSink() {
        RuntimeContextSupport contextSupport = Mockito.mock(RuntimeContextSupport.class);
        Db db = Mockito.mock(Db.class);

        Mockito.when(contextSupport.getDb()).thenReturn(db);

        AbstractSink firstSink = pointsModule.createSink(PointsSink.class, contextSupport);
        AbstractSink secondSink = pointsModule.createSink(PointsSink.class, contextSupport);
        Assertions.assertNotEquals(firstSink, secondSink);
    }

    @Test
    void createProcessor() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);
        SignalCollector signalCollector = Mockito.mock(SignalCollector.class);

        Mockito.when(contextSupport.getSignalCollector()).thenReturn(signalCollector);

        PointRule rule = Mockito.mock(PointRule.class);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> firstProcessor = pointsModule.createProcessor(rule, contextSupport);
        AbstractProcessor<? extends AbstractRule, ? extends Signal> secondProcessor = pointsModule.createProcessor(rule, contextSupport);

        Assertions.assertNotEquals(firstProcessor, secondProcessor);
    }

    @Test
    void createProcessorForUnknownRule() {
        RuleExecutionContextSupport contextSupport = Mockito.mock(RuleExecutionContextSupport.class);

        Assertions.assertNull(pointsModule.createProcessor(new UnknownRule("abc"), contextSupport));
    }

    private static class UnknownRule extends AbstractRule {
        public UnknownRule(String id) {
            super(id);
        }
    }
}