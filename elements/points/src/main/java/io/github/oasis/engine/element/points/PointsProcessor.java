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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.TimeOffset;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static io.github.oasis.core.utils.Constants.COLON;

/**
 * @author Isuru Weerarathna
 */
public class PointsProcessor extends AbstractProcessor<PointRule, PointSignal> {
    public PointsProcessor(Db dbPool, RuleContext<PointRule> ruleContext) {
        super(dbPool, ruleContext);
    }

    @Override
    public boolean isDenied(Event event, ExecutionContext context) {
        return super.isDenied(event, context) || !isCriteriaSatisfied(event, rule, context);
    }

    @Override
    protected void beforeEmit(PointSignal signal, Event event, PointRule rule, ExecutionContext context, DbContext db) {
        // do nothing
    }

    @Override
    public List<PointSignal> process(Event event, PointRule rule, ExecutionContext context, DbContext db) {
        BigDecimal score;
        if (rule.isAwardBasedOnEvent()) {
            score = rule.getAmountExpression().resolve(event, context);
        } else {
            score = rule.getAmountToAward();
        }

        if (rule.isCapped()) {
            String baseKey = PointIDs.getGameUserPointsSummary(event.getGameId(), event.getUser());
            TimeOffset tcx = new TimeOffset(event.getTimestamp(), context.getUserTimeZone());
            String childKey = PointsSink.RULE_PFX + rule.getPointId() + COLON + tcx.getByType(rule.getCapDuration());
            BigDecimal residue = db.incrementCapped(score, baseKey, childKey, rule.getCapLimit());
            System.out.println(residue);
            if (Numbers.isNegative(residue)) {
                // cannot increment due to limit already exceeded. Hence skipping.
                return null;
            } else {
                score = residue;
            }
        }
        return Collections.singletonList(new PointSignal(rule.getId(), rule.getPointId(), score, event));
    }

    private boolean isCriteriaSatisfied(Event event, PointRule rule, ExecutionContext context) {
        return rule.getCriteria() == null || rule.getCriteria().matches(event, rule, context);
    }
}
