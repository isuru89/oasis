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

package io.github.oasis.engine.processors;

import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.PointRule;
import io.github.oasis.engine.rules.signals.PointSignal;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class PointsProcessor extends AbstractProcessor<PointRule, PointSignal> {
    public PointsProcessor(Db dbPool, RuleContext<PointRule> ruleContext) {
        super(dbPool, ruleContext);
    }

    @Override
    public boolean isDenied(Event event) {
        return super.isDenied(event) || !isCriteriaSatisfied(event, rule);
    }

    @Override
    protected void beforeEmit(PointSignal signal, Event event, PointRule rule, DbContext db) {
        // do nothing
    }

    @Override
    public List<PointSignal> process(Event event, PointRule rule, DbContext db) {
        if (rule.isAwardBasedOnEvent()) {
            BigDecimal score = rule.getAmountExpression().apply(event, rule);
            return Collections.singletonList(new PointSignal(rule.getId(), score, event));
        } else {
            return Collections.singletonList(new PointSignal(rule.getId(), rule.getAmountToAward(), event));
        }
    }

    private boolean isCriteriaSatisfied(Event event, PointRule rule) {
        return rule.getCriteria() == null || rule.getCriteria().test(event, rule);
    }
}
