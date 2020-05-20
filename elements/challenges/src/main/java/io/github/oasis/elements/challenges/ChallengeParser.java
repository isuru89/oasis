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

import io.github.oasis.core.VariableNames;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractElementParser;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.Scripting;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Numbers;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeParser extends AbstractElementParser {

    @Override
    public AbstractDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, ChallengeDef.class);
    }

    @Override
    public AbstractRule convert(AbstractDef definition) {
        if (definition instanceof ChallengeDef) {
            return toRule((ChallengeDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    private ChallengeRule toRule(ChallengeDef def) {
        String id = def.generateUniqueHash();
        ChallengeRule rule = new ChallengeRule(id);
        AbstractDef.defToRule(def, rule);

        rule.setStartAt(Numbers.ifNull(def.getStartAt(), Constants.DEFAULT_START_TIME));
        rule.setExpireAt(Numbers.ifNull(def.getExpireAt(), Constants.DEFAULT_EXPIRE_TIME));
        rule.setWinnerCount(Numbers.ifNull(def.getWinnerCount(), Constants.DEFAULT_WINNER_COUNT));

        rule.setCriteria(EventExecutionFilterFactory.create(def.getCriteria()));

        rule.setPointId(def.getPointId());
        Object pointAwards = def.getPointAwards();
        if (Objects.nonNull(pointAwards)) {
            if (pointAwards instanceof Number) {
                rule.setAwardPoints(BigDecimal.valueOf(((Number) pointAwards).doubleValue()));
            } else {
                rule.setCustomAwardPoints(Scripting.create((String) pointAwards, Constants.VARIABLE_POSITION, VariableNames.RULE_VAR));
            }
        } else {
            rule.setCustomAwardPoints(null);
        }

        if (Objects.nonNull(def.getScope())) {
            String type = (String) def.getScope().getOrDefault(Constants.DEF_SCOPE_TYPE, Constants.DEFAULT_SCOPE.toString());
            long scopeId = (long) def.getScope().getOrDefault(Constants.DEF_SCOPE_ID, Constants.DEFAULT_SCOPE_VALUE);
            rule.setScope(ChallengeRule.ChallengeScope.valueOf(type));
            rule.setScopeId(scopeId);
        } else {
            rule.setScope(Constants.DEFAULT_SCOPE);
        }

        return rule;
    }
}
