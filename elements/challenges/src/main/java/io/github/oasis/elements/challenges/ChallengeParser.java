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
import io.github.oasis.core.elements.*;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.challenges.spec.ChallengeSpecification;
import io.github.oasis.elements.challenges.spec.ScopeDef;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeParser extends AbstractElementParser {

    @Override
    public AbstractDef<? extends BaseSpecification> parse(EngineMessage persistedObj) {
        ChallengeDef def = loadFrom(persistedObj, ChallengeDef.class);
        def.validate();
        return def;
    }

    @Override
    public AbstractRule convert(AbstractDef<? extends BaseSpecification> definition) {
        if (definition instanceof ChallengeDef) {
            return toRule((ChallengeDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    @Override
    public AcceptedDefinitions getAcceptingDefinitions() {
        return new AcceptedDefinitions().addAcceptingDefinition(
                ChallengesModule.ID,
                new AcceptedDefinition()
                        .setDefinitionClz(ChallengeDef.class)
                        .setSpecificationClz(ChallengeSpecification.class)

        );
    }

    private ChallengeRule toRule(ChallengeDef def) {
        def.validate();

        ChallengeRule rule = new ChallengeRule(def.getId());
        AbstractDef.defToRule(def, rule);

        rule.setStartAt(Numbers.ifNull(def.getSpec().getStartAt(), Constants.DEFAULT_START_TIME));
        rule.setExpireAt(Numbers.ifNull(def.getSpec().getExpireAt(), Constants.DEFAULT_EXPIRE_TIME));
        rule.setWinnerCount(Numbers.ifNull(def.getSpec().getWinnerCount(), Constants.DEFAULT_WINNER_COUNT));

        PointAwardDef pointAwards = def.getSpec().getRewards().getPoints();
        rule.setPointId(def.getSpec().getRewards().getPoints().getId());
        if (pointAwards.getAmount() != null) {
            rule.setAwardPoints(pointAwards.getAmount());
        } else {
            rule.setCustomAwardPoints(Scripting.create(pointAwards.getExpression(), Constants.VARIABLE_POSITION, VariableNames.RULE_VAR));
        }

        ScopeDef scopeTo = def.getSpec().getScopeTo();
        if (Objects.nonNull(scopeTo)) {
            rule.setScope(ChallengeRule.ChallengeScope.valueOf(scopeTo.getType()));
            if (scopeTo.getTargetId() != null) {
                rule.setScopeId(scopeTo.getTargetId());
            }
            if (Utils.isNotEmpty(scopeTo.getTargetIds())) {
                rule.setScopeIds(Set.copyOf(scopeTo.getTargetIds()));
            }
        } else {
            rule.setScope(Constants.DEFAULT_SCOPE);
        }

        return rule;
    }
}
