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

import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.ActorSignalCollector;
import io.github.oasis.engine.elements.AbstractProcessor;
import io.github.oasis.engine.elements.badges.BadgeConditionalProcessor;
import io.github.oasis.engine.elements.badges.rules.BadgeRule;
import io.github.oasis.engine.elements.badges.rules.BadgeConditionalRule;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.external.Db;

/**
 * @author Isuru Weerarathna
 */
public class BadgeFactories {

    public static AbstractProcessorFactory<? extends BadgeRule> conditionalBadgeProcessor() {
        return new ConditionalBadges();
    }

    private static class ConditionalBadges extends AbstractProcessorFactory<BadgeConditionalRule> {

        @Override
        public AbstractProcessor<BadgeConditionalRule, ? extends Signal> create(BadgeConditionalRule rule, ActorSignalCollector collector, Db db) {
            return new BadgeConditionalProcessor(db, new RuleContext<>(rule, collector));
        }
    }

}
